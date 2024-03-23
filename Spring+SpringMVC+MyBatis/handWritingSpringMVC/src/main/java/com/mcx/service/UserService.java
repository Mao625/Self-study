package com.mcx.service;

import com.mcx.pojo.User;

import java.util.List;

public interface UserService {
    List<User> findUsers(String name);
}
