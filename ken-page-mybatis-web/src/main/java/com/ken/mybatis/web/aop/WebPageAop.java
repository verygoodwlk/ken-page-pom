package com.ken.mybatis.web.aop;

import com.ken.mybatis.protocol.BasePage;
import com.ken.mybatis.protocol.BaseResult;
import com.ken.mybatis.utils.KenPages;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;


/**
 * Controller层的分页拦截处理的AOP
 */
@Aspect
public class WebPageAop {

    @Value("${kenplugin.page.key.num:pageNum}")
    private String pNum;
    @Value("${kenplugin.page.key.size:pageSize}")
    private String pSize;

    /**
     * 被@Paging注解标记的方法，会被AOP拦截
     * @return
     */
//    @Around("@annotation(org.springframework.web.bind.annotation.RestController)")
    @Around("@within(org.springframework.web.bind.annotation.RestController) || @within(org.springframework.stereotype.Controller)")
    public Object pageHandler(ProceedingJoinPoint joinPoint) throws Throwable {
        //获得请求对象
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        //获得请求参数
        String pageNum = request.getParameter(pNum);
        String pageSize = request.getParameter(pSize);

        //封装Page对象
        BasePage page = null;
        if (pageNum != null && pageSize != null) {
            //缓存到ThreadLocal中
            KenPages.setPage(Integer.valueOf(pageNum), Integer.valueOf(pageSize));
        }

        //放行请求
        Object result = null;
        try {
            result = joinPoint.proceed();

            //获取请求线程中Page对象
            page = KenPages.getPage();
            if (page != null) {
                //如果有分页信息，则修改返回对象
                if (result instanceof BaseResult) {
                    //将page分页设置到返回对象上
                    ((BaseResult)result).setPage(page);
                }
                //业务执行 清空分页缓存
                KenPages.clearPage();
            }
        } catch (Throwable throwable) {
            //异常不处理，继续上抛
            throw throwable;
        }

        return result;
    }
}
