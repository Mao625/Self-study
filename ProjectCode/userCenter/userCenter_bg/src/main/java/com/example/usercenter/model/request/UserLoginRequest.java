package com.example.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {


    private static final long serialVersionUID = -1352380448886358649L;

    private String userAccount;
    private String userPassword;
}
