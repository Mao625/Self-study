package com.example.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenter.model.domain.User;
import com.example.usercenter.mapper.UserMapper;
import com.example.usercenter.service.UserService;
import org.springframework.stereotype.Service;

/**
* @author MCX
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2024-07-23 18:04:45
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

}




