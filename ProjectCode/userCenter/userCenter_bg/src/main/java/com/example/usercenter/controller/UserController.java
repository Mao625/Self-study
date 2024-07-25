package com.example.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.usercenter.model.domain.User;
import com.example.usercenter.model.request.UserLoginRequest;
import com.example.usercenter.model.request.UserRegisterRequest;
import com.example.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.usercenter.contant.UserConstant.*;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @GetMapping("/hello")
    public String sayHello(){
        return "hello Word!";
    }

    @PostMapping("/register")
    public Long userRegister(@RequestBody UserRegisterRequest registerRequest){
        if(registerRequest == null){
            return null;
        }

        String userAccount = registerRequest.getUserAccount();
        String userPassword = registerRequest.getUserPassword();
        String checkPassword = registerRequest.getCheckPassword();
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            return null;
        }
        return userService.userRegister(userAccount, userPassword, checkPassword);
    }

    @PostMapping("/login")
    public User userLogin(@RequestBody UserLoginRequest loginRequest, HttpServletRequest request){
        if(loginRequest == null){
            return null;
        }

        String userAccount = loginRequest.getUserAccount();
        String userPassword = loginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount,userPassword)){
            return null;
        }
        return userService.userLogin(userAccount, userPassword, request);
    }

    @GetMapping("/search")
    public List<User> searchUser(String userName,HttpServletRequest request){
        if(!isAdmin(request)){
            log.warn("search fail is not admin");
            return new ArrayList<>();
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNoneBlank(userName)){
            queryWrapper.like("userName",userName);
        }

        List<User> userList = userService.list(queryWrapper);
        userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return userList;
    }

    @DeleteMapping()
    public boolean deleteUser(Long id,HttpServletRequest request){
        if(id <= 0){
            log.warn("id <= 0");
            return false;
        }
        if(!isAdmin(request)){
            log.warn("delete fail is not admin");
            return false;
        }

        return userService.removeById(id);
    }

    private boolean isAdmin(HttpServletRequest request){
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATUS);
        User user = (User) attribute;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }
}
