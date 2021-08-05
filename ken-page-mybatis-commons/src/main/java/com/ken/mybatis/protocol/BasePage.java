package com.ken.mybatis.protocol;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 抽象分页的父类
 */
@Deprecated
public abstract class BasePage<T> {

    /**
     * 几个抽象的设置方法
     * @param pageNum
     */
    public abstract void setPageNum(Integer pageNum);
    public abstract void setPageSize(Integer pageSize);
    public abstract void setCount(Integer count);
    public abstract void setTotal(Integer total);

    @JsonIgnore
    public abstract Integer getPageNum();
    @JsonIgnore
    public abstract Integer getPageSize();
    @JsonIgnore
    public abstract Integer getCount();
    @JsonIgnore
    public abstract Integer getTotal();
}
