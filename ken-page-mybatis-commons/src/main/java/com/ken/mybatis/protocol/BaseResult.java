package com.ken.mybatis.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Map;

/**
 * 接口返回统一对象
 */
//为null的值不参与json序列化
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResult<T> implements Serializable {

    /**
     * 分页对象
     */
    private Map<String, String> page;

    /**
     * 无参构造方法
     */
    public BaseResult() {
    }

    public Map<String, String> getPage() {
        return page;
    }

    public void setPage(Map<String, String> page) {
        this.page = page;
    }
}
