package com.mcx.springmvc.servlet;

import com.mcx.springmvc.annotation.Controller;
import com.mcx.springmvc.annotation.RequestMapping;
import com.mcx.springmvc.annotation.RequestParam;
import com.mcx.springmvc.context.WebApplicationContext;
import com.mcx.springmvc.exception.ContentException;
import com.mcx.springmvc.handler.MyHandler;
import org.dom4j.DocumentException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.TypeVariable;
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
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        excuteDispacth(req,resp);
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

            }
        } catch (Exception e) {
        throw new RuntimeException(e);
    }
    }
    /**
     * 根据请求的路径找到对应的controller，处理器映射器
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
}
