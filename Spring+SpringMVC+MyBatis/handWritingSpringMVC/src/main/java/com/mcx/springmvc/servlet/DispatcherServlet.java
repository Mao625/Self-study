package com.mcx.springmvc.servlet;

import com.mcx.springmvc.annotation.Controller;
import com.mcx.springmvc.annotation.RequestMapping;
import com.mcx.springmvc.context.WebApplicationContext;
import com.mcx.springmvc.exception.ContentException;
import com.mcx.springmvc.handler.MyHandler;
import org.dom4j.DocumentException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {
    WebApplicationContext webApplicationContext;

    //保存请求映射关系
    List<MyHandler> myHandlerList = new ArrayList<MyHandler>();

    @Override
    public void init() throws ServletException {
        // classpath: springmvc.xml
        String contextConfigLocation = this.getServletConfig().getInitParameter("contextConfigLocation");
         webApplicationContext = new WebApplicationContext(contextConfigLocation);
        try {
            //初始化容器
            webApplicationContext.refresh();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //初始化请求映射
        initHandlerMapping();
        System.out.println("请求映射关系："+myHandlerList);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

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
}
