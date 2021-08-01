package com.ken.mybatis.plugin;

import com.ken.mybatis.utils.MyBatisUtils;
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
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Statement;
import java.util.List;

/**
 * SQL语句记录插件、耗时
 */
@Intercepts({
        @Signature(
                type = StatementHandler.class,
                method = "query",
                args = {Statement.class, ResultHandler.class}
        ),
        @Signature(
                type = StatementHandler.class,
                method = "queryCursor",
                args = {Statement.class}
        ),
        @Signature(
                type = StatementHandler.class,
                method = "update",
                args = {Statement.class}
        )
})
public class SQLExecPlugin implements Interceptor {

    //日志对象
    private Logger log = LoggerFactory.getLogger(SQLExecPlugin.class);

    /**
     * 插件拦截方法
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        //获取非代理的目标对象
        StatementHandler statementHandler = (StatementHandler) MyBatisUtils.getNoProxyTraget(invocation.getTarget());
//        System.out.println("Mybatis插件获取的当前对象：" + statementHandler);
        //获得当前执行的SQL语句
        MetaObject statmentObject = SystemMetaObject.forObject(statementHandler);
        //获取全局配置对象
        Configuration configuration = (Configuration) statmentObject.getValue("delegate.configuration");
        //获得需要编译的sql语句
        BoundSql boundSql = (BoundSql) statmentObject.getValue("delegate.boundSql");
        //获得sql
        String sql = boundSql.getSql().toLowerCase().trim().replace("\n", "");

        //记录当前的sql语句
        log.info("[SQL] executor - [" + sql + "]");


        //获取所有的参数
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        Object parameterObject = boundSql.getParameterObject();
        if (parameterMappings != null) {
            for(int i = 0; i < parameterMappings.size(); ++i) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() != ParameterMode.OUT) {
                    String propertyName = parameterMapping.getProperty();
                    Object value;
                    if (boundSql.hasAdditionalParameter(propertyName)) {
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (parameterObject == null) {
                        value = null;
                    } else {
                        MetaObject metaObject = configuration.newMetaObject(parameterObject);
                        value = metaObject.getValue(propertyName);
                    }
                    log.info("[SQL] params[" + i + "] - [" + propertyName + ":" + value + "]");
                }
            }
        }

        //执行目标方法
        //记录执行前的时间
        long beginTime = System.currentTimeMillis();
        Object result = invocation.proceed();
        long end = System.currentTimeMillis();
        //计算耗时
        double tTime = new BigDecimal(end - beginTime).divide(new BigDecimal(1000)).setScale(6, RoundingMode.DOWN).doubleValue();
        log.info("[SQL] take up time - [" + tTime + "s]");

        return result;
    }
}
