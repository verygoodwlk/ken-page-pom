package com.ken.mybatis.protocol;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 抽象分页的父类
 */
public abstract class BasePage<T> {

    /**
     * 分页默认关闭
     */
    @JsonIgnore
    private boolean enable = false;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * 几个抽象的设置方法
     * @param pageNum
     */
    public abstract void setPageNum(Integer pageNum);
    public abstract void setPageSize(Integer pageSize);
    public abstract void setCount(Integer count);
    public abstract void setTotal(Integer total);

    public abstract Integer getPageNum();
    public abstract Integer getPageSize();
    public abstract Integer getCount();
    public abstract Integer getTotal();
}
