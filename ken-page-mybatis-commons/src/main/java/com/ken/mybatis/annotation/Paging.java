package com.ken.mybatis.annotation;

import java.lang.annotation.*;

/**
 * 分页注解 标记该注解的方法表示后续的业务需要进行分页处理
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Paging {
}
