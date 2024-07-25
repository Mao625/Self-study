package com.example.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -4853927297482331694L;

    private String userAccount;
    private String userPassword;
    private String checkPassword;
}
