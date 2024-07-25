package com.example.usercenter.service;
import java.util.Date;

import com.example.usercenter.mapper.UserMapper;
import com.example.usercenter.model.domain.User;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
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

    @Test
    void userRegister() {
        String userAccount = "";
        String userPassword = "12345678";
        String checkPassword = "12345678";

        //非空测试
        long rgFlag1 = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1,rgFlag1);

        //账户长度不小于四位
        userAccount = "mcx";
        long rgFlag2 = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1,rgFlag2);

        //密码不小于八位
        userAccount = "mcx01";
        userPassword = "123456";
        checkPassword = "123456";
        long rgFlag3 = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1,rgFlag3);

        //账户包含特殊字符
        userAccount = "m cx01";
        userPassword = "12345678";
        checkPassword = "12345678";
        long rgFlag4 = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1,rgFlag4);

        //账户不能重复
        userAccount = "admin";
        userPassword = "12345678";
        checkPassword = "12345678";
        long rgFlag5 = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1,rgFlag5);

        //密码和校验密码不相同
        userAccount = "mcx01";
        userPassword = "12345678";
        checkPassword = "123456789";
        long rgFlag6 = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1,rgFlag6);

        //插入数据
        userAccount = "mcx01";
        userPassword = "12345678";
        checkPassword = "12345678";
        long rgFlag7 = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertTrue(rgFlag7 > 0);
    }
}
