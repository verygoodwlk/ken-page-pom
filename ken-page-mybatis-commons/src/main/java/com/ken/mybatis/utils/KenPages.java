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

    public static void setPage(Page page, boolean isBatch){
        if (page != null) {
            page.setEnable(!isBatch);
        }
        KenPages.pageThreadLocal.set(page);
    }

    public static void setPage(Page page) {
        KenPages.setPage(page, false);
    }
}
