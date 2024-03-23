package com.mcx.service.impl;

import com.mcx.pojo.User;
import com.mcx.service.UserService;
import com.mcx.springmvc.annotation.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    public List<User> findUsers(String name) {
        System.out.println("查询的名字为："+name);
        //模拟数据
        List<User> users = new ArrayList<User>();
        users.add(new User(1,"老王","1234"));
        users.add(new User(2,"老张","6789"));

        return users;
    }
}
