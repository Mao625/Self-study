package com.example.usercenter.service;

import com.example.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.naming.spi.StateFactory;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
* @author MCX
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2024-07-23 18:04:45
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    long userRegister(String userAccount,String userPassword,String checkPassword);

    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User getSafetyUser(User selectOne);

}
