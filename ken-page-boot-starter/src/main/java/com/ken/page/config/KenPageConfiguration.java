package com.ken.page.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KenPageConfiguration {

    @Configuration
    @ConfigurationProperties(prefix = "kenplugin.execsql")
    public static class ExecSqlConfiguration {
        /**
         * 是否开启sql记录插件
         */
        boolean enable;
    }

    @Configuration
    @ConfigurationProperties(prefix = "kenplugin.page")
    public static class PageConfiguration {
        /**
         * 是否开启分页插件
         */
        boolean enable;
    }

    @Configuration
    @ConfigurationProperties(prefix = "kenplugin.page.webconfig")
    public static class WebPageConfiguration {
        /**
         * 是否开启Web层兼容分页插件
         */
        boolean enable;
    }


    @Configuration
    @ConfigurationProperties(prefix = "kenplugin.page.key")
    public static class WebPageParamConfiguration {
        /**
         * 当前页的名称
         */
        String num;
        /**
         * 每页多少条
         */
        String size;
        /**
         * 总页码
         */
        String total;
        /**
         * 总条数
         */
        String count;
    }

    @Configuration
    @ConfigurationProperties(prefix = "kenplugin.auto.mapping")
    public static class AutoMappingConfiguration {
        /**
         * 是否开启自动化映射
         */
        boolean enable;
    }
}
