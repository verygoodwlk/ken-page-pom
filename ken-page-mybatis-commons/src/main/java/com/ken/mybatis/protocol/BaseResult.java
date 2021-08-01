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
     * 返回码
     */
    protected Integer code;

    /**
     * 返回消息
     */
    protected String message;

    /**
     * 数据部分
     */
    private T data;

    /**
     * 分页对象
     */
    private Page<T> page;

    /**
     * 操作成功的静态方法
     * @param data
     * @param <T>
     * @return
     */
    public static <T> BaseResult<T> createSucc(T data){
        return new BaseResult<>(200,"操作成功", data, null);
    }

    /**
     * 其他状态的静态方法
     * @param data
     * @param <T>
     * @return
     */
    public static <T> BaseResult<T> create(Integer code, String message, T data){
        return new BaseResult<>(code,message, data, null);
    }

    /**
     * 全参数构造方法
     * @param code
     * @param message
     * @param data
     * @param page
     */
    public BaseResult(Integer code, String message, T data, Page<T> page) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.page = page;
    }

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

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
