package com.ken.mybatis.utils;


import com.ken.mybatis.entity.Page;
import com.ken.mybatis.protocol.BasePage;

/**
 * Page对象缓存的ThreadLocal工具类
 */
public class KenPages {

    private static ThreadLocal<BasePage> pageThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<Boolean> autoThreadLocal = new ThreadLocal<>();

    /*
    分页相关的方法
     */
    public static BasePage getPage(){
        return pageThreadLocal.get();
    }

    public static void setPage(Integer pageNum, Integer pageSize, boolean isBatch){
        BasePage page = SpringContextHolder.getBean(BasePage.class);
        if (page == null) {
            page = new Page();
        }
        page.setPageNum(pageNum);
        page.setPageSize(pageSize);
        page.setEnable(!isBatch);
        KenPages.pageThreadLocal.set(page);
    }

    public static void setPage(Integer pageNum, Integer pageSize) {
        KenPages.setPage(pageNum, pageSize, false);
    }

    public static void clearPage(){
        KenPages.pageThreadLocal.set(null);
    }

    /*
    结果集自动映射相关的方法
     */
    public static Boolean getAutoFlag(){
        return autoThreadLocal.get();
    }

    public static void setAutoFlag(Boolean autoFlag) {
        autoThreadLocal.set(autoFlag);
    }
}
