package com.ken.mybatis.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ken.mybatis.entity.Page;

import java.io.Serializable;

/**
 * 接口返回统一对象
 */
//为null的值不参与json序列化
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResult<T> implements Serializable {

    /**
     * 分页对象
     */
    private Page<T> page;

    /**
     * 无参构造方法
     */
    public BaseResult() {
    }

    public Page<T> getPage() {
        return page;
    }

    public void setPage(Page<T> page) {
        this.page = page;
    }
}
