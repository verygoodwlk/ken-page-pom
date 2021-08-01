package com.ken.mybatis.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;

/**
 * 分页对象
 */
public class Page<T> implements Serializable {

    /**
     * 分页默认关闭
     */
    @JsonIgnore
    private boolean enable = false;
    /**
     * 当前页码
     */
    private Integer pageNum = 1;

    /**
     * 每页显示多少条
     */
    private Integer pageSize = 10;

    /**
     * 记录总条数
     */
    private Integer count;

    /**
     * 总页数
     */
    private Integer totle;


    public Page setCount(Integer count) {
        this.count = count;
        if (count != null && pageSize != null)
            this.totle = count % pageSize == 0 ? count / pageSize : count / pageSize + 1;
        return this;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getCount() {
        return count;
    }

    public Integer getTotle() {
        return totle;
    }

    public void setTotle(Integer totle) {
        this.totle = totle;
    }
}
