package com.ken.mybatis.utils;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

public class MyBatisUtils {

    /**
     * 获得最终的目标对象
     * @return
     */
    public static Object getNoProxyTraget(Object traget){
        //进行对象的绑定
        MetaObject metaObject = SystemMetaObject.forObject(traget);
        while(metaObject.hasGetter("h")){
            traget = metaObject.getValue("h.target");
            metaObject = SystemMetaObject.forObject(traget);
        }
        return traget;
    }
}
