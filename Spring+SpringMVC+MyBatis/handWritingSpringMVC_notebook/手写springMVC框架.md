# 手写springMVC框架

 [参考视频](https://www.bilibili.com/video/BV18K4y1v7f7?p=8&spm_id_from=pageDriver&vd_source=0203de8f93e0b02a0b58ceba9217649c)

[参考文档](https://juejin.cn/post/7139807630024769549)

sprigMVC执行流程

![image-20240324145946739](手写springMVC框架.assets/image-20240324145946739.png)

## 1、创建项目

![image-20240321113241441](C:\Users\MCX\AppData\Roaming\Typora\typora-user-images\image-20240321113241441.png)



## 2、导入坐标

```xml
<dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.12</version>
      <scope>test</scope>
    </dependency>

    <dependency>
      <groupId>javax.servlet</groupId>
      <artifactId>javax.servlet-api</artifactId>
      <version>3.1.0</version>
    </dependency>

    <!--解析配置文件-->
    <dependency>
      <groupId>dom4j</groupId>
      <artifactId>dom4j</artifactId>
      <version>1.6.1</version>
    </dependency>
    <dependency>
      <groupId>org.apache.commons</groupId>
      <artifactId>commons-lang3</artifactId>
      <version>3.4</version>
    </dependency>
```







## 3、创建业务逻辑层和控制层

![image-20240321115636829](C:\Users\MCX\AppData\Roaming\Typora\typora-user-images\image-20240321115636829.png)

## 4、创建常用注解

![image-20240321120143896](C:\Users\MCX\AppData\Roaming\Typora\typora-user-images\image-20240321120143896.png)



## 5、创建spring容器

### 1、修改web.xml文件、创建springmvc.xml

[Spring的IOC容器为什么用反射而不用new来创建实例](https://blog.csdn.net/qq_45076180/article/details/117733342)

**web.xml**

```xml
<!DOCTYPE web-app PUBLIC
 "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"
 "http://java.sun.com/dtd/web-app_2_3.dtd" >

<web-app>
  <display-name>Archetype Created Web Application</display-name>

  <servlet>
    <servlet-name>DispatcherServlet</servlet-name>
    <servlet-class>com.springmvc.servlet.DispatcherServlet</servlet-class>
    <!--springMVC配置文件-->
    <init-param>
      <param-name>contextConfigLocation</param-name>
      <param-value>classpath:springmvc.xml</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
  </servlet>


  <servlet-mapping>
    <servlet-name>DispatcherServlet</servlet-name>
    <url-pattern>/</url-pattern>
  </servlet-mapping>


</web-app>
```

**springmvc.xml**

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<beans>
    <component-scan base-pack="com.mcx.service,com.mcx.controller"></component-scan>
</beans>

```



### 2、创建DispatcherServlet继承于HttpServlet

实现三个方法

![image-20240321121752317](C:\Users\MCX\AppData\Roaming\Typora\typora-user-images\image-20240321121752317.png)

### 3、创建webapplication容器

目的是将要管理的bean放到容器里，所以要解析xml文件，扫描包下的类

**定义的webapplication容器类，将解析得到的类名放到classNameList中**



**过程**：首先要扫描springMvc.xml文件下定义的包，通过解析后得到包下面的类名，然后根据类名通过反射创建对象。



```java
package com.mcx.springmvc.context;

import com.mcx.springmvc.xml.XmlParse;
import org.dom4j.DocumentException;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WebApplicationContext {
    String contextConfigLocation;

    List<String> classNameList = new ArrayList<String>();

    public WebApplicationContext(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }

    /**
     * 初始化容器
     * @throws DocumentException
     */
    public void refresh() throws DocumentException {
        //解析xml文件，得到 "com.mcx.service,com.mcx.controller"
        String aPackage = XmlParse.getPackage(contextConfigLocation.split(":")[1]);
        String[] packs = aPackage.split(",");
        if (packs.length >0){
            for (String s : packs){
                excuteScannerPackage(s);
            }
        }
        System.out.println("解析后的类为："+classNameList);
    }

    /**
     * 得到包下面对应的类
     * @param pack
     */
    public void excuteScannerPackage(String pack){
        URL url = this.getClass().getClassLoader().getResource("/" + pack.replaceAll("\\.", "/"));
        System.out.println("url:"+url);
        String urlFile = url.getFile();
        System.out.println("urlfile:"+urlFile);
        File dire = new File(urlFile);
        for(File f: dire.listFiles()){
            if(f.isDirectory()){
                excuteScannerPackage(pack + "." + f.getName());
            }
            else {
                String className = pack + "." + f.getName().replaceAll(".class", "");
                classNameList.add(className);
            }
        }
    }
}

```

解析xml文件的工具类

```java
package com.mcx.springmvc.xml;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.net.URL;

/**
 * 解析xml文件,得到扫描的包名。package：com.mcx.service,com.mcx.controller
 */
public class XmlParse {
    public static String getPackage(String xml) throws DocumentException {
        // 使用dom4j解析xml文件
        SAXReader saxReader = new SAXReader();
        InputStream resourceAsStream = XmlParse.class.getClassLoader().getResourceAsStream(xml);
        System.out.println("InputStream:"+resourceAsStream);
        //得到xml对象
        Document document = saxReader.read(resourceAsStream);
        Element rootElement = document.getRootElement();
        Element element = rootElement.element("component-scan");
        Attribute attribute = element.attribute("base-pack");
        System.out.println("atttibute:"+attribute);
        String text = attribute.getText();
        return text;
    }
}

```



通过反射创建类的对象

```java
    /**
     * 通过反射创建类的对象
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public void excuteInstent() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if(classNameList.size() == 0){
            throw new ContentException("没有要实例化的类");
        }
        else {
            System.out.println("开始实例化对象");
            for(String c : classNameList){
                Class<?> aClass = Class.forName(c);
                if (aClass.isAnnotationPresent(Controller.class)){
                    String beanName = aClass.getSimpleName().substring(0,1).toLowerCase() + aClass.getSimpleName().substring(1);
                    iocNAme.put(beanName,aClass.newInstance());
                }
                else if(aClass.isAnnotationPresent(Service.class)){
                    //com.mcx.service.impl.UserServiceImpl
                    Service serviceName = aClass.getAnnotation(Service.class);
                    String beanName = serviceName.value();
                    if("".equals(beanName)){
                        Class<?>[] interfaces = aClass.getInterfaces();
                        for (Class<?> i : interfaces){
                            String name = i.getSimpleName().substring(0,1).toLowerCase() + i.getSimpleName().substring(1);
                            iocNAme.put(name,aClass.newInstance());
                        }
                    }
                    else {
                        iocNAme.put(beanName,aClass.newInstance());
                    }
                }
            }
        }
    }
```

## 6、spring容器中对象的注入

对controller层中@Autowired修饰的属性按类型或者名称注入

```java
/**
     * 对controller层中进行属性注入
     * @throws IllegalAccessException
     */
    public void excuteAutowired() throws IllegalAccessException {
        if(iocMap.isEmpty()){
            throw new ContentException("没有要注入的对象");
        }
        else {
            for(Map.Entry<String,Object> entry : iocMap.entrySet()){
                String key = entry.getKey();
                Object bean = entry.getValue();
                System.out.println("bean="+bean);
                Field[] declaredFields = bean.getClass().getDeclaredFields();
                for(Field field : declaredFields){
                    if ((field.isAnnotationPresent(Autowired.class))){
                        Autowired annotation = field.getAnnotation(Autowired.class);
                        String beanName = annotation.value();
                        if("".equals(beanName)){
                            Class<?> type = field.getType();
                            beanName = type.getSimpleName().substring(0,1).toLowerCase() + type.getSimpleName().substring(1);
                        }   
                        //使用爆破强制获取私有属性
                        field.setAccessible(true);
                        //属性注入
                        System.out.println("iocMap.get(beanName)="+iocMap.get(beanName));
                        System.out.println("field属性注入前="+field);
                        field.set(bean,iocMap.get(beanName));
                        System.out.println("field属性注入后="+field);
                    }
                }
            }
        }
    }
```



## 7、初始化请求映射

扫描ioc容器中controller层的bean，接着找到被@RequestMapping修饰的方法，拿到它的value值，最后将 url、object、method，c存储到定义的MyHandler对象中

```java
 //保存请求映射关系
    List<MyHandler> myHandlerList = new ArrayList<MyHandler>(); 

/**
     * 初始化请求映射
     */
    public void initHandlerMapping(){
        String classUrl = "";
        if(webApplicationContext.iocMap.isEmpty()){
            throw new ContentException("Spring容器为空");
        }
        else {
            for(Map.Entry<String,Object> entry : webApplicationContext.iocMap.entrySet()){
                Class<?> cls = entry.getValue().getClass();
                if(cls.isAnnotationPresent(Controller.class)){
                    if(cls.isAnnotationPresent(RequestMapping.class)){
                        RequestMapping requestMappingAnnotation = cls.getAnnotation(RequestMapping.class);
                        //得到controller类上的请求路径
                        classUrl = requestMappingAnnotation.value();
                    }
                    Method[] declaredMethods = cls.getDeclaredMethods();
                    for(Method method : declaredMethods){
                        if(method.isAnnotationPresent(RequestMapping.class)){
                            RequestMapping methodAnnotation = method.getAnnotation(RequestMapping.class);
                            String url = methodAnnotation.value();
                            MyHandler myHandler = new MyHandler(classUrl + url, entry.getValue(), method);
                            myHandlerList.add(myHandler);
                        }
                    }
                }
            }
        }
    }
```



## 8、请求的分发

### 1、根据请求的路径找到对应的handler

```java
  /**
     * 根据请求的路径找到对应的controller
     * @param req
     * @return
     */
    public MyHandler getController(HttpServletRequest req){
        String requestURI = req.getRequestURI();
        for (MyHandler myHandler : myHandlerList){
            //如果根据请求路径找到对应的controller方法则返回
            if(myHandler.getUrl().equals(requestURI)){
                return myHandler;
            }
        }
        return null;
    }
```

### 2、找到handler后设置请求的参数，利用反射调用方法，把参数也传递给controller中的方法

```java
  /**
     * 分发请求
     * @param req
     * @param resp
     */
    public void excuteDispacth(HttpServletRequest req, HttpServletResponse resp){
        MyHandler myHandler = getController(req);
        try{
            if(myHandler == null){
                resp.getWriter().print("<h1> 404 NOT FOUND </h1>");
            }
            else {
                Class<?>[] parameterTypes = myHandler.getMethod().getParameterTypes();

                //定义参数数组
                Object[] param = new Object[parameterTypes.length];

                //获取请求中的参数集合
                Map<String, String[]> parameterMap = req.getParameterMap();
                
                //迭代前端页面请求的参数
                for (Map.Entry<String,String[]> entry : parameterMap.entrySet()){
                    //后期待优化
                    String key = entry.getKey();
                    String name = entry.getValue()[0];
                    param[2] = name;
                }
                param[0] = req;
                param[1] = resp;

                //使用反射调用对应方法
                myHandler.getMethod().invoke(myHandler.getController(),param);
            }
        } catch (Exception e) {
        throw new RuntimeException(e);
    }
    }
```



### 3、@RequestParam的反射，以及形参赋值的优化

**分发请求**

```java
    /**
     * 分发请求
     * @param req
     * @param resp
     */
    public void excuteDispacth(HttpServletRequest req, HttpServletResponse resp){
        //找到handler
        MyHandler myHandler = getController(req);
        try{
            if(myHandler == null){
                resp.getWriter().print("<h1> 404 NOT FOUND </h1>");
            }
            else {
                Class<?>[] parameterTypes = myHandler.getMethod().getParameterTypes();

                //定义参数数组
                Object[] param = new Object[parameterTypes.length];

                for (int i = 0;i < parameterTypes.length;i++){
                    if(parameterTypes[i].getSimpleName().equals("HttpServletRequest")){
                        param[i] = req;
                    } else if (parameterTypes[i].getSimpleName().equals("HttpServletResponse")) {
                        param[i] = resp;
                    }
                }


                //获取请求中的参数集合
                Map<String, String[]> parameterMap = req.getParameterMap();

                //迭代前端页面请求的参数
                for (Map.Entry<String,String[]> entry : parameterMap.entrySet()){
                    String key = entry.getKey();
                    String value = entry.getValue()[0];
                    int index = hasRequestParam(myHandler.getMethod(), key);
                    if(index != -1){
                        param[index] = value;
                    }
                    else {
                        //如果没有使用@RequestParam注解绑定形参，则根据名称传递参数
                        List<String> methodParam = getMethodParam(myHandler.getMethod());
                        for (int i = 0; i < methodParam.size(); i++) {
                            if(methodParam.get(i).equals(key)){
                                param[i] = value;
                            }
                        }
                    }
                }

                //使用反射调用对应方法
                myHandler.getMethod().invoke(myHandler.getController(),param);
            }
        } catch (Exception e) {
        throw new RuntimeException(e);
    }
    }
```



**反射RequestParam**

```java
 /**
     * 判断控制层方法有没有 @RequestParam修饰的形参，如果有，并且value值与浏览器请求的参数名一致，则返回参数位置
     * @param method
     * @param name
     * @return
     */
    public int hasRequestParam(Method method,String name){
        Parameter[] parameters = method.getParameters();
        for(int i =0 ;i < parameters.length;i++){
            if (parameters[i].isAnnotationPresent(RequestParam.class)){
                RequestParam annotation = parameters[i].getAnnotation(RequestParam.class);
                String paramName = annotation.value();
                if (paramName.equals(name)){
                    return i;
                }
            }
        }
        return -1;
    }
```

**返回方法中形参的名字**

```java
/**
     * 返回方法中的形参的名字
     * @param method
     * @return
     */
    public List<String> getMethodParam(Method method){
        List<String> paramList = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            paramList.add(parameters[i].getName());
        }
        System.out.println("paramList="+paramList);
        return paramList;
    }
```



## 9、控制器返回值是String的处理

需要在方法调用后选择转向还是重定向

**controller**

```java

    @RequestMapping("/getByName")
    public String getUserMessage(HttpServletRequest request, HttpServletResponse response,  String name) throws IOException {
        response.setContentType("text/html;charset = utf-8");
        String userName = userService.getMessage(name);
        request.setAttribute("userMessage",userName);
        //转发到 user.jsp，调用完方法后要跳转到user.jsp
        return "forward:/user.jsp";
    }
```

**jsp文件**

```jsp

<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<html>
<head>
    <title>user.jsp</title>
</head>
<body>
<h1>${requestScope.userMessage}</h1>
</body>
</html>

```

**DispacherServlet**

```java
                //使用反射调用对应方法
                Object result = myHandler.getMethod().invoke(myHandler.getController(), param);

                if(result instanceof String){
                    //跳转jsp
                    String viewName = (String) result;
                    //forward:/user.jsp
                    if(viewName.contains(":")){
                        String viewType = viewName.split(":")[0];
                        String viewPage = viewName.split(":")[1];
                        if(viewType.equals("forward")){
                            //转发
                            req.getRequestDispatcher(viewPage).forward(req,resp);
                        }
                        else {
                            //重定向
                            resp.sendRedirect(viewPage);
                        }
                    }
                    // 什么都没有写，默认转发
                    else {
                        req.getRequestDispatcher(viewName).forward(req,resp);
                    }
                }
```



## 10、返回值是JSON的处理

**controller中**

```java
 @RequestMapping("/getAll")
    @RequestBody
    public List<User> getAll(HttpServletRequest request, HttpServletResponse response,  String name) throws IOException {
        return userService.findUsers(name);
    }
```

**DispacherServlet**

```java
else {
                    //返回的是JSON
                    Method method = myHandler.getMethod();
                    if(method.isAnnotationPresent(RequestBody.class)){
                        //调用JSON转换工具，将返回值转换
                        ObjectMapper objectMapper = new ObjectMapper();
                        String json = objectMapper.writeValueAsString(result);
                        //设置编码
                        resp.setContentType("text/html:charset=utf-8");
                        PrintWriter writer = resp.getWriter();
                        writer.print(json);
                        writer.flush();
                        writer.close();
                    }
                }
```

