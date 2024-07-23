package com.example.usercenter.service;
import java.util.Date;

import com.example.usercenter.mapper.UserMapper;
import com.example.usercenter.model.domain.User;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Test
    public void addTest(){
        User user = new User();
        user.setUserName("mcx");
        user.setUserAccount("admin");
        user.setAvatarUrl("data:image/png;base64");
        user.setGender(0);
        user.setUserPassword("123456");
        user.setPhone("12345678901");
        user.setEmail("1234@qq.com");
        boolean saveFlag = userService.save(user);

        Assert.assertEquals(true,saveFlag);
        System.out.println(user.getId());


    }

}
