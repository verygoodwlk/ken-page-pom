package com.ken.mybatis.plugin;

import com.ken.mybatis.entity.Page;
import com.ken.mybatis.utils.KenPages;
import com.ken.mybatis.utils.MyBatisUtils;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 自定义分页插件
 */
@Intercepts(
        @Signature(
                type = StatementHandler.class,
                method = "prepare",
                args = {Connection.class, Integer.class}
        )
)
public class PagePlugin implements Interceptor {

    //日志对象
    private Logger log = LoggerFactory.getLogger(PagePlugin.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //获取非代理的目标对象
        StatementHandler statementHandler = (StatementHandler) MyBatisUtils.getNoProxyTraget(invocation.getTarget());
//        System.out.println("Mybatis插件获取的当前对象：" + statementHandler);
        //获得当前执行的SQL语句
        MetaObject statmentObject = SystemMetaObject.forObject(statementHandler);
        //获得需要编译的sql语句
        BoundSql boundSql = (BoundSql) statmentObject.getValue("delegate.boundSql");
        //获得sql
        String sql = boundSql.getSql().toLowerCase().trim().replace("\n", "").replaceAll("\\s+", " ");

        String totalSql = sql;
        //判断该sql是否为查询语句
        if(!sql.startsWith("select")){
            //不是查询语句，无需分页
            return invocation.proceed();
        }

        //获取分页的Page对象并且是开启了分页的，否则无法分页
        Page page = KenPages.getPage();
        if (page == null || !page.isEnable()) {
            //找不到分页对象，无法分页
            return invocation.proceed();
        }
        log.debug("[PAGING SQL] paging begin...");
        log.debug("[PAGING SQL] paging sql - [" + sql + "]");

        //判断该sql语句是否包含limit
        int selectIndex = 0;
        int limitIndex = -1;
        int paramBegin = -1;
        int paramEnd = -1;
        boolean isSunSql = false;
        if((limitIndex = sql.indexOf("limit")) != -1){
            isSunSql = true;
            //包含指定分页形式，截取需要分页的主sql
            selectIndex = sql.lastIndexOf("select", limitIndex);
            //截取主sql语句
            totalSql = sql.substring(selectIndex, limitIndex);
            log.debug("[PAGING SQL] has substring page sql - [{}]", totalSql);

            //判断totalSql中是否存在参数?
            int bIndex = 0;//记录开始的位置
            int pIndex = -1;//参数的下标
            int pCount = 0;//参数的数量
            while ((pIndex = totalSql.indexOf("?", bIndex)) != -1) {
                pCount++;
                bIndex = pIndex + 1;
            }

            if (pCount > 0) {
                //如果存在参数，则要判断这些参数在原SQL语句中的位置
                bIndex = selectIndex;
                int bpCount = 0;
                while ((pIndex = sql.lastIndexOf("?", bIndex)) != -1) {
                    bpCount++;
                    bIndex = pIndex - 1;
                }

                paramBegin = bpCount;
                paramEnd = bpCount + pCount - 1;
                log.debug("[PAGING SQL] substring page sql param beging - [{}]", paramBegin);
                log.debug("[PAGING SQL] substring page sql param end - [{}]", paramEnd);
            }
        }


        //调用封装的方法，获取当前查询的总条数
        int count = getTotal(invocation, statmentObject, totalSql, isSunSql, paramBegin, paramEnd);
        page.setCount(count);
        //设置总页码
        if (page.getPageSize() == null || page.getPageSize() <= 0) page.setPageSize(10);//如果没有每页显示的条数，默认设置为10条
        page.setTotal(page.getCount() % page.getPageSize() == 0 ?
                page.getCount() / page.getPageSize() :
                page.getCount() / page.getPageSize() + 1);

        //调整page的页码
        if (page.getPageNum() <= 0) page.setPageNum(1);
        if (page.getPageNum() > page.getTotal()) page.setPageNum(page.getTotal());

        //开始分页
        //去除最后的分号
        if(sql.endsWith(";")){
            sql = sql.substring(0, sql.length() - 1);
        }

        //组装分页属性
        String limit = " limit " +  ((page.getPageNum() - 1) * page.getPageSize()) + "," + page.getPageSize();

        //如果存在子SQL，需要将sql设置回原sql
        if (isSunSql) {
            sql = sql.replaceAll("\\s*limit\\s+\\?", limit);
        } else {
            //拼接limit关键字
            sql += limit;
        }

        //回设最新的SQL语句
        statmentObject.setValue("delegate.boundSql.sql", sql);
        log.debug("[PAGING SQL] exec sql - {}", sql);

        //放行，进行sql编译
        PreparedStatement ps = (PreparedStatement) invocation.proceed();
        log.debug("[PAGING SQL] paging end...");
        //返回statement对象交给MyBatis进行后续的sql执行操作
        return ps;
    }

    /**
     * 计算共有多少条记录
     * @return
     */
    private Integer getTotal(Invocation invocation, MetaObject statmentObject, String sql, boolean isSunSql, int beginParams, int endParams){
        //获得参数管理器
        ParameterHandler ph = (ParameterHandler) statmentObject.getValue("delegate.parameterHandler");

        //拼接sql - 获得计算总数的sql语句
        //select count(1) from student where age = ? and sex = ?
        int formIndex = sql.indexOf("from");
        String countsql = "select count(1) as total " + sql.substring(formIndex);
        //去除order by - 提升查询条数的性能
        int orderbyIndex = -1;
        if((orderbyIndex = countsql.indexOf("order by")) != -1){
            countsql = countsql.substring(0,  orderbyIndex);
        }
        //获得计算总数的sql
        log.debug("[PAGING SQL] paging count sql - " + countsql);

        //执行该sql语句

        //获得Connection链接
        Connection conn = (Connection) invocation.getArgs()[0];
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(countsql);
            if (!isSunSql) {
                //通过参数处理器设置参数
                ph.setParameters(ps);
            } else if (beginParams != -1 && endParams != -1){
                //需要手动设置参数
                BoundSql boundSql = (BoundSql) statmentObject.getValue("delegate.boundSql");
                //获取全局配置对象
                Configuration configuration = (Configuration) statmentObject.getValue("delegate.configuration");
                TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
                //获得参数列表
                List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
                Object parameterObject = boundSql.getParameterObject();

                for (int i = beginParams; i <= endParams; i++){
                    ParameterMapping parameterMapping = parameterMappings.get(i);
                    if (parameterMapping.getMode() != ParameterMode.OUT) {
                        Object value;
                        String propertyName = parameterMapping.getProperty();
                        if (boundSql.hasAdditionalParameter(propertyName)) { // issue #448 ask first for additional params
                            value = boundSql.getAdditionalParameter(propertyName);
                        } else if (parameterObject == null) {
                            value = null;
                        } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                            value = parameterObject;
                        } else {
                            MetaObject metaObject = configuration.newMetaObject(parameterObject);
                            value = metaObject.getValue(propertyName);
                        }
                        TypeHandler typeHandler = parameterMapping.getTypeHandler();
                        JdbcType jdbcType = parameterMapping.getJdbcType();
                        if (value == null && jdbcType == null) {
                            jdbcType = configuration.getJdbcTypeForNull();
                        }
                        try {
                            log.debug("[PAGING SQL] paging count sql params" + ((i - beginParams) + 1) + " - [" + value + "]");
                            typeHandler.setParameter(ps, (i - beginParams) + 1, value, jdbcType);
                        } catch (TypeException | SQLException e) {
                            throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
                        }
                    }
                }
            }
            //执行sql语句
            rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(rs != null){
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if(ps != null){
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}
