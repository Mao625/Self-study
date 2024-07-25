package com.example.usercenter.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenter.model.domain.User;
import com.example.usercenter.mapper.UserMapper;
import com.example.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.xml.crypto.dsig.spec.XPathFilterParameterSpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @author MCX
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2024-07-23 18:04:45
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {
    @Autowired
    private UserMapper userMapper;

    //设置盐，混淆密码
    private final static String SALT = "mcx";

    //定义返回用户消息的建
    private final static String USER_LOGIN_STATUS = "currentUser";


    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            return -1;
        }
        if(userAccount.length()<4) {
            return -1;
        }
        if(userPassword.length() < 8 || checkPassword.length() < 8){
            return -1;
        }
        //账户名校验
        String  patten = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(patten).matcher(userAccount);
        if(matcher.find()){
            return -1;
        }
        //账户不能重复
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount",userAccount);
        long count = this.count(userQueryWrapper);
        if(count > 0){
            return -1;
        }

        if(!userPassword.equals(checkPassword)){
            return -1;
        }

        //密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveFlag = this.save(user);
        if(!saveFlag){
            return -1;
        }

        return user.getId();
    }

    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1、信息校验
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            return null;
        }
        if(userAccount.length()<4) {
            return null;
        }
        if(userPassword.length() < 8){
            return null;
        }
        //账户名校验
        String  patten = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(patten).matcher(userAccount);
        if(matcher.find()){
            return null;
        }

        //2、密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount",userAccount);
        userQueryWrapper.eq("userPassword",encryptPassword);
        User selectOne = userMapper.selectOne(userQueryWrapper);
        if(selectOne == null){
            log.info("user login fail, userAccount cannot match userPassword");
            return null;
        }
        //3、用户脱敏
        User saftyUser = new User();
        saftyUser.setId(selectOne.getId());
        saftyUser.setUserName(selectOne.getUserName());
        saftyUser.setUserAccount(selectOne.getUserAccount());
        saftyUser.setAvatarUrl(selectOne.getAvatarUrl());
        saftyUser.setGender(selectOne.getGender());
        saftyUser.setPhone(selectOne.getPhone());
        saftyUser.setEmail(selectOne.getEmail());
        saftyUser.setUserStatus(selectOne.getUserStatus());
        saftyUser.setCreateTime(selectOne.getCreateTime());

        //4、记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATUS,saftyUser);

        return saftyUser;
    }
}




