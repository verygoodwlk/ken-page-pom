package com.ken.mybatis.plugin;

import com.ken.mybatis.entity.Page;
import com.ken.mybatis.utils.KenPages;
import com.ken.mybatis.utils.MyBatisUtils;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        String sql = boundSql.getSql().toLowerCase().trim().replace("\n", "");;

        String pageSql = sql;
        //处理SQL的关键词情况@{}
        int beginIndex = sql.indexOf("@{");
        int endIndex = sql.indexOf("}");
        if(beginIndex != -1 && endIndex != -1) {
            //获取需要分页的sql
            sql = sql.substring(beginIndex + 2, endIndex);
        }

        //判断该sql是否为查询语句
        if(!sql.startsWith("select")){
            //不是查询语句，无需分页
            return invocation.proceed();
        }

        //判断该sql语句时候包含limit
        if(sql.indexOf("limit") != -1){
            //自带分页，无需插件分页
            return invocation.proceed();
        }

        //获取分页的Page对象并且是开启了分页的，否则无法分页
        Page page = KenPages.getPage();
        if (page == null || !page.isEnable()) {
            //找不到分页对象，无法分页
            return invocation.proceed();
        }
        log.info("[PAGING SQL] paging begin...");
        log.info("[PAGING SQL] paging sql - [" + sql + "]");

        //调用封装的方法，获取当前查询的总条数
        int count = getTotal(invocation, statmentObject, sql);
        page.setCount(count);

        //开始分页
        //去除最后的分号
        if(sql.endsWith(";")){
            sql = sql.substring(0, sql.length() - 1);
        }
        //拼接limit关键字
        sql += " limit " +  (page.getPageNum() - 1) * page.getPageSize() + "," + page.getPageSize();

        //如果原sql中包含@{}字符，需要用调整后的sql替换掉
        if(beginIndex != -1 && endIndex != -1) {
            pageSql = pageSql.replaceAll("@\\{(.|\\n|\\r|\\s)*\\}", sql);
        } else {
            pageSql = sql;
        }

        //回设sql语句
        log.info("[PAGING SQL] paging sql update - " + pageSql);
        statmentObject.setValue("delegate.boundSql.sql", pageSql);

        //放行，进行sql编译
        PreparedStatement ps = (PreparedStatement) invocation.proceed();
//        //获得参数的总数 , 计算的是?的个数
//        int paramsCount = ps.getParameterMetaData().getParameterCount();
//        ps.setInt(paramsCount-1, (page.getPageNum() - 1) * page.getPageSize());
//        ps.setInt(paramsCount, page.getPageSize());
        log.info("[PAGING SQL] paging end...");
        //返回statement对象交给MyBatis进行后续的sql执行操作
        return ps;
    }

    /**
     * 计算共有多少条记录
     * @return
     */
    private Integer getTotal(Invocation invocation, MetaObject statmentObject, String sql){
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
        log.info("[PAGING SQL] paging count sql - " + countsql);

        //执行该sql语句

        //获得Connection链接
        Connection conn = (Connection) invocation.getArgs()[0];
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(countsql);
            //通过参数处理器设置参数
            ph.setParameters(ps);
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
