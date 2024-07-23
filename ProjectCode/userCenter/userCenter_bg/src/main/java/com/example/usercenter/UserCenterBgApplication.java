package com.example.usercenter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.usercenter.mapper")
public class UserCenterBgApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserCenterBgApplication.class, args);
    }

}
