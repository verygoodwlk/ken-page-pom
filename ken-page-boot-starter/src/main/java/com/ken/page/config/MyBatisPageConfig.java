package com.ken.page.config;

import com.ken.mybatis.aop.PageAop;
import com.ken.mybatis.plugin.PagePlugin;
import com.ken.mybatis.plugin.SQLExecPlugin;
import com.ken.mybatis.web.aop.WebPageAop;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

@Configurable
public class MyBatisPageConfig {

    /**
     * 装配Mybatis SQL执行记录插件
     */
    @Bean
    //有mybatis的环境
    @ConditionalOnClass(SqlSessionFactory.class)
    //指定属性值为true时才能加载
    @ConditionalOnProperty(prefix = "kenplugin.execsql.enable", havingValue = "true", matchIfMissing = true)
    public Interceptor getExecSqlPlugin(){
        return new SQLExecPlugin();
    }

    /**
     * 装配Mybatis 分页插件
     */
    @Bean
    //有mybatis的环境
    @ConditionalOnClass(SqlSessionFactory.class)
    //指定属性值为true时才能加载
    @ConditionalOnProperty(prefix = "kenplugin.page.enable", havingValue = "true", matchIfMissing = true)
    public Interceptor getPagePlugin(){
        return new PagePlugin();
    }

    /**
     * 装配aop
     */
    @Bean
    //存在aop环境
    @ConditionalOnBean(Aspect.class)
    //指定属性值为true时才能加载
    @ConditionalOnProperty(prefix = "kenplugin.page.enable", havingValue = "true", matchIfMissing = true)
    public PageAop getPageAop(){
        return new PageAop();
    }

    /**
     * 装配aop
     */
    @Bean
    //存在aop环境
    //当存在分页插件时，才会加载该AOP
    @ConditionalOnBean(value = {Aspect.class,PagePlugin.class})
    //web环境才能加载
    @ConditionalOnWebApplication
    //指定属性值为true时才能加载，缺省不加载
    @ConditionalOnProperty(prefix = "kenplugin.page.webconfig.enable", havingValue = "true", matchIfMissing = false)
    public WebPageAop getWebPageAop(){
        return new WebPageAop();
    }
}
