package com.mcx.springmvc.handler;

import java.lang.reflect.Method;

public class MyHandler {
    private String url;
    private Object controller;
    private Method method;

    public MyHandler(String url, Object controller, Method method) {
        this.url = url;
        this.controller = controller;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "MyHandler{" +
                "url='" + url + '\'' +
                ", controller=" + controller +
                ", method=" + method +
                '}';
    }
}
