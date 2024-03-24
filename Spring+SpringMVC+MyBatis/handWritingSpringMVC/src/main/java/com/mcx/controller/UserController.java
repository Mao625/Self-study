package com.mcx.controller;

import com.mcx.pojo.User;
import com.mcx.service.UserService;
import com.mcx.springmvc.annotation.*;

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
    public void getUsers(HttpServletRequest request, HttpServletResponse response,  String name) throws IOException {
        response.setContentType("text/html;charset = utf-8");
        List<User> users = userService.findUsers(name);
        PrintWriter out =  response.getWriter();
        out.println("<h1>springMVC控制器"+name+"</h1>");


    }

    @RequestMapping("/getByName")
    public String getUserMessage(HttpServletRequest request, HttpServletResponse response,  String name) throws IOException {
        response.setContentType("text/html;charset = utf-8");
        String userName = userService.getMessage(name);
        request.setAttribute("userMessage",userName);
        //转发到 user.jsp，调用完方法后要跳转到user.jsp
        return "forward:/user.jsp";
    }


    @RequestMapping("/getAll")
    @RequestBody
    public List<User> getAll(HttpServletRequest request, HttpServletResponse response,  String name) throws IOException {
        return userService.findUsers(name);
    }
}
