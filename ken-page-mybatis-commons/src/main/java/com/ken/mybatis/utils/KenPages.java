package com.ken.mybatis.utils;


import com.ken.mybatis.entity.Page;

/**
 * Page对象缓存的ThreadLocal工具类
 */
public class KenPages {

    private static ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();

    public static Page getPage(){
        return pageThreadLocal.get();
    }

    public static void setPage(Page page){
        KenPages.pageThreadLocal.set(page);
    }
}
