package com.mcx.springmvc.context;


import com.mcx.springmvc.annotation.Autowired;
import com.mcx.springmvc.annotation.Controller;
import com.mcx.springmvc.annotation.Service;
import com.mcx.springmvc.exception.ContentException;
import com.mcx.springmvc.xml.XmlParse;
import org.dom4j.DocumentException;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebApplicationContext {
    String contextConfigLocation;

    //保存包下的类名
    List<String> classNameList = new ArrayList<String>();

    //保存通过反射创建的对象
    public Map<String, Object> iocMap = new ConcurrentHashMap<String, Object>();

    public WebApplicationContext(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }

    /**
     * 容器的初始化，完成对象的创建
     * @throws DocumentException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public void refresh() throws DocumentException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        //调用工具类 XmlParse 得到包名组成的字符串数组
        String Packages = XmlParse.getPackage(contextConfigLocation.split(":")[1]);
        String[] packageList = Packages.split(",");

        //扫描包下的所有类，并存储到classNameList中
        if (packageList.length > 0) {
            for (String p : packageList) {
                excuteScannerPackage(p);
            }
        }
        System.out.println("包下的所有类为：" + classNameList);

        //根据得到的类利用反射创建对象，并保存到iocMap中
        getObject();
        System.out.println("类的对象为："+iocMap);


        //针对Autowired注解修饰的属性进行 属性注入
        excuteAutowired();
    }

    /**
     * 扫描包下的类，并保存到 classNameList 中
     *
     * @param pack
     */
    public void excuteScannerPackage(String pack) {
        //com.mcx.service
        URL url = this.getClass().getClassLoader().getResource("/" + pack.replaceAll("\\.", "/"));
        String urlFile = url.getFile();
        File file = new File(urlFile);
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                String packName = pack + "." + f.getName();
                excuteScannerPackage(packName);
            } else {
                String packageName = pack + "." + f.getName().replaceAll(".class", "");
                classNameList.add(packageName);
            }
        }
    }


    /**
     * 通过反射创建类的对象
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public void getObject() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (classNameList.size() == 0) {
            throw new ContentException("没有要实例化的类");
        } else {
            for (String cls : classNameList) {
                Class<?> aClass = Class.forName(cls);
                if (aClass.isAnnotationPresent(Controller.class)) {
                    String beanName = aClass.getSimpleName().substring(0, 1).toLowerCase() + aClass.getSimpleName().substring(1);
                    iocMap.put(beanName, aClass.newInstance());
                } else if (aClass.isAnnotationPresent(Service.class)) {
                    Service serviceName = aClass.getAnnotation(Service.class);
                    String beanName = serviceName.value();
                    if ("".equals(beanName)) {
                        Class<?>[] interfaces = aClass.getInterfaces();
                        for (Class<?> I : interfaces) {
                            String name = I.getSimpleName().substring(0, 1).toLowerCase() + I.getSimpleName().substring(1);
                            iocMap.put(name, aClass.newInstance());
                        }
                    } else {
                        iocMap.put(beanName, aClass.newInstance());
                    }

                }

            }
        }
    }

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

                        //如果value值为空，则按类型装配
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
}
