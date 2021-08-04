package com.ken.mybatis.annotation;

import java.lang.annotation.*;

/**
 * 自动映射注解，标记该注解的方法会自动映射结果集
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AutoMapping {
}
