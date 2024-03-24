package com.mcx.controller;

import com.mcx.pojo.User;
import com.mcx.service.UserService;
import com.mcx.springmvc.annotation.Autowired;
import com.mcx.springmvc.annotation.Controller;
import com.mcx.springmvc.annotation.RequestMapping;
import com.mcx.springmvc.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    @RequestMapping("/list")
    public void getUsers(HttpServletRequest request, HttpServletResponse response, @RequestParam("name") String name) throws IOException {
        response.setContentType("text/html;charset = utf-8");
        List<User> users = userService.findUsers(name);
        PrintWriter out =  response.getWriter();
        out.println("<h1>springMVC控制器"+name+"</h1>");


    }
}
