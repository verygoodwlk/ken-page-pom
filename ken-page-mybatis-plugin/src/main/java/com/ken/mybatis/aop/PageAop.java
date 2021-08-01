package com.ken.mybatis.aop;

import com.ken.mybatis.entity.Page;
import com.ken.mybatis.utils.KenPages;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * 分页的AOP拦截
 */
@Aspect
public class PageAop {

    /**
     * 被@Paging注解标记的方法，会被AOP拦截
     * @return
     */
    @Around("@annotation(com.ken.mybatis.annotation.Paging)")
    public Object pageHandler(ProceedingJoinPoint joinPoint) throws Throwable {

        //判断是否存在Page对象，如果不存在就直接创建
        Page page = KenPages.getPage();
        if (page != null)
            //开启分页
            page.setEnable(true);


        //放行请求
        Object result = null;
        try {
            result = joinPoint.proceed();

            //关闭分页
            if (page != null)
                page.setEnable(false);
        } catch (Throwable throwable) {
            //异常不处理，继续上抛
            throw throwable;
        }

        return result;
    }
}
