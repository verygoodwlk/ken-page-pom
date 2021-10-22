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

    /**
     * 查询主表from的位置
     * @param begin - 开始查询的位置
     * @param sql - 需要查询的sql语句
     * @return
     */
    public static int selectFromIndex(int begin, String sql){
        int count = 0;//括号的数量
        int fromIndex = sql.indexOf("from", begin);

        //搜索位置
        int selectIndex = fromIndex;
        int bIndex = -1;
        while((bIndex = sql.lastIndexOf("(", selectIndex)) != -1){
            count++;
            selectIndex = bIndex - 1;
        }

        //搜索反括号
        selectIndex = fromIndex;
        int eIndex = -1;
        while((eIndex = sql.lastIndexOf(")", selectIndex)) != -1){
            count--;
            selectIndex = eIndex - 1;
        }

        if (count == 0) {
            return fromIndex;
        } else {
            return selectFromIndex(fromIndex + 1, sql);
        }
    }

    public static void main(String[] args) {
        String sql1 = "select p.pr_id,\n" +
                "               if(p.pr_pid is not null, (select pr_name from sys_power where pr_id = p.pr_pid), '顶级权限') as prPname,\n" +
                "               p.pr_name,\n" +
                "               p.pr_flag,\n" +
                "               p.create_time,(select pr_name from sys_power where pr_id = p.pr_pid)(select pr_name from sys_power where pr_id = p.pr_pid)\n" +
                "               if(p.status = 0, '一级菜单', if(p.status = 1, '二级菜单', '页面权限'))\n" +
                "        from sys_power p where id = (select cid from table2)";

        String sql2 = "select * from table where id = (select cid from table2)";

        String testSql = sql1;

        int index = selectFromIndex(0, testSql);
        String countsql = "select count(1) as total " + testSql.substring(index);
        System.out.println(countsql);
    }

}
