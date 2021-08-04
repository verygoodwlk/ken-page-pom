package com.ken.mybatis.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ToMore {

    /**
     * 子集的实际对象类型
     * @return
     */
    Class type();
}
