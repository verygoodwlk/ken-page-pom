package com.ken.page.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.ken.mybatis.aop.AutoMappingAop;
import com.ken.mybatis.aop.PageAop;
import com.ken.mybatis.plugin.PagePlugin;
import com.ken.mybatis.plugin.ResuletAutoPlugin;
import com.ken.mybatis.plugin.SQLExecPlugin;
import com.ken.mybatis.utils.SpringContextHolder;
import com.ken.mybatis.web.aop.WebPageAop;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

@Configurable
public class MyBatisPageConfig {

    @Bean
    public SpringContextHolder getSpringContextHolder(){
        return new SpringContextHolder();
    }

    /**
     * 装配Mybatis SQL执行记录插件
     */
    @Bean
    //有mybatis的环境
    @ConditionalOnClass(SqlSessionFactory.class)
    //指定属性值为true时才能加载
    @ConditionalOnProperty(prefix = "kenplugin.execsql", value = "enable", havingValue = "true", matchIfMissing = true)
    public SQLExecPlugin getExecSqlPlugin(){
        return new SQLExecPlugin();
    }

    /**
     * 装配Mybatis 分页插件
     */
    @Bean
    //有mybatis的环境
    @ConditionalOnClass(SqlSessionFactory.class)
    //指定属性值为true时才能加载
    @ConditionalOnProperty(prefix = "kenplugin.page", value = "enable", havingValue = "true", matchIfMissing = true)
    public PagePlugin getPagePlugin(){
        return new PagePlugin();
    }

    /**
     * 装配Mybatis 结果集自动映射插件
     * @return
     */
    @Bean
    //有Mybatis 和 MybatisPlus的环境才能使用
    @ConditionalOnBean({SqlSessionFactory.class, MybatisPlusAutoConfiguration.class})
    //指定属性值为true时才能加载,缺省时不加载
    @ConditionalOnProperty(prefix = "kenplugin.auto.mapping", value = "enable", havingValue = "true", matchIfMissing = false)
    public ResuletAutoPlugin getAutoPlugin(){
        return new ResuletAutoPlugin();
    }

    /**
     * 自动映射的AOP
     */
    @Bean
    //有插件时才会加载
    @ConditionalOnBean(ResuletAutoPlugin.class)
    public AutoMappingAop getAutoMappingAop(){
        return new AutoMappingAop();
    }

    /**
     * 装配aop
     */
    @Bean
    //存在aop环境
//    @ConditionalOnBean(Aspect.class)
    //指定属性值为true时才能加载
    @ConditionalOnProperty(prefix = "kenplugin.page", value = "enable", havingValue = "true", matchIfMissing = true)
    public PageAop getPageAop(){
        return new PageAop();
    }

    /**
     * 装配aop
     */
    @Bean
    //存在aop环境
    //当存在分页插件时，才会加载该AOP
    @ConditionalOnBean(PagePlugin.class)
    //web环境才能加载
    @ConditionalOnWebApplication
    //指定属性值为true时才能加载，缺省不加载
    @ConditionalOnProperty(prefix = "kenplugin.page.webconfig", value = "enable", havingValue = "true", matchIfMissing = false)
    public WebPageAop getWebPageAop(){
        return new WebPageAop();
    }
}
