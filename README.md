[TOC]

### 一、ken-page
> 1、一款基于Mybatis的分页插件    
> 2、可以兼容原生MyBatis、tk-Mybatis以及Mybatis-Plus等主流框架  
> 3、解决复杂关联查询导致分页错误的问题
> 4、配合Web层拦截，实现无侵入式分页

### 二、SQL执行监控功能

> 插件自带SQL执行记录以及耗时监控功能，所有运行的sql语句以及相关参数还有耗时时间都将被日志记录

```
[SQL] executor - [select * from test where id = ?]
[SQL] params[0] - [id:5]
[SQL] take up time - [0.013s]
```

关闭SQL执行监控，只需要在application.yml配置即可

```yml
#关闭SQL记录，默认开启
kenplugin:
 execsql:
  enable: false
```

### 三、分页基本使用

添加依赖

```xml
<dependency> 
   <groupId>com.ken</groupId>
   <artifactId>live-data-entity-base</artifactId>
   <version>1.1</version>
</dependency>
```

开启分页 - 在业务合适的地方，调用以下代码开启分页

```java
//pageNum - 当前页码
//pageSize - 每页显示多少条
//注意：这行代码后，当前业务的所有数据库查询操作都将自动实现分页效果
KenPages.setPage(new Page(pageNum, pageSize));
```

查询后获取分页信息(总条数、总页码等数据)

```java
//分页的相关信息
Page page = KenPages.getPage();
int count = page.getCount(); //获得总记录数
int totle = page.getTotle(); //总页数
```

> 注意：因为分页插件的无侵入设计，可以随时停止所有分页的功能。只需在application.yml中配置即可
```yml
#关闭分页功能 - 分页的业务会自动变成查询全部，默认开启
kenplugin:
 page:
  enable: false
```


### 四、复杂业务定制化分页

> 在一些复杂的业务中，可能存在多个子业务，有些子业务可能需要分页，有些子业务不需要，则可以使用定制化分页的功能

开启分页定制化

```java
//参数二：表示开启定制化分页，默认关闭
KenPages.setPage(new Page(pageNum, pageSize), true);
```

标记分页注解 - 通常在子业务方法或者mapper接口方法上标记

```java
主业务方法(){
   //开启分页
   KenPages.setPage(new Page(pageNum, pageSize), true);
   //子业务1方法
   //子业务2方法
   //子业务3方法
   //子业务4方法
}

子业务1方法(){
}

@Paging
子业务2方法(){
}

子业务3方法(){
}

@Paging
子业务4方法(){
}
```
> 当前模式下，只有标注了@Paging注解的业务，才会受到分页的影响     
> @Paging注解可以直接标记在Mapper接口的方法上

### 五、多表关联SQL分页

> 实际开发过程中，SQL可能会关联多张表查询，导致查出来的分页结果会存在数量不足的问题，比如

```sql
select * from 
    table1 t join table2 t2 on t1.id = t2.tid
```

> 以上这条SQL，本意是讲table1作为主表，关联查询出table2的结果，table2的结果作为子集放入到table1对应的记录中。       
> 此时，如果要分页，应该是对table1表进行分页，但是因为关联的存在，实际table1查询出来的记录，并不会满足当前页的记录数。

**解决方案**

> 我们需要调整下SQL的结构

```sql
select * from
  (@{select * from table1 limit ?}) t join table2 t2 on t.id = t2.tid 
```
> table1作为主表，我们可以人为将主表的查询部分独立出来，然后在主表查询前后加上@{}，
> 插件会自动判别到该部分为主表查询，会先对该部分分页，再将分页出来的结果去关联其他表，
> 这样就会得到正确的分页结果。实际情况可能比这个复杂的多，所以需要开发者根据实际的业务去动态调整SQL语句的结构。

> 注意：@{}在Mybatis的映射文件中可能会报错，那是因为@{}被MyBatis的dtd约束文件判定为不合法的字符，这里开发者无需在意，
> 该错误并不影响SQL的执行

### 六、Web层自动化兼容分页

开启Web层分页兼容配置（默认关闭）

```yml
#开启web层分页兼容配置
kenplugin:
 page:
  webconfig:
   enable: true
```

客户端传递分页参数

```http request
#pageNum - 当前页
#pageSize - 每页显示多少条
http://server.com/xxxx?pageNum=1&pageSize=5
```

> 开启web层兼容分页后，只需要客户端传递分页参数，后续业务会自动化完成分页，
> 无需再使用KenPages.setPage(new Page(pageNum, pageSize));方法来开启分页。
> 
>业务复杂的情况下，仍然可以使用@Paging注解实现定制化分页

定制分页参数名称

> 当不想使用pageNum和pageSize作为参数名称时，可以定制名称

```yml
kenplugin:
 page:
  key:
   #定制当前页的参数名称
   num: pageNo
   #定制每页条数的参数名称
   size: pageS
```

返回分页信息给客户端

> Web层（Controller方法）返回的对象必须继承BaseResult类，比如

```java
class OutPut<T> extends BaseResult{
    Integer code;
    String msg;
    T datas;
}

@RequestMapping("/xxx/xxx")
public OutPut<List<A>> xxxx(){
}
```
> 客户端会收到如下的结果数据：

```json
{
    //分页信息
    "page": {
        "pageNum": 1,
        "pageSize": 2,
        "count": 25,
        "totle": 13
    },
    "code": 200,
    "message": "请求成功",
    "data": [
        {
            //数据部分....
        }
    ]
}
```