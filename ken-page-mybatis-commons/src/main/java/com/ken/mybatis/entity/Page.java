package com.ken.mybatis.entity;

import com.ken.mybatis.protocol.BasePage;

import java.io.Serializable;

/**
 * 分页对象
 */
public class Page<T> extends BasePage<T> implements Serializable {

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
    private Integer total;

    public Page() {
    }

    public Page(Integer pageNum, Integer pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public void setCount(Integer count) {
        this.count = count;
        if (count != null && pageSize != null)
            this.total = count % pageSize == 0 ? count / pageSize : count / pageSize + 1;
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

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
