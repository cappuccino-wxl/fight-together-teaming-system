package com.example.match.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 用户注册请求体
 * @date: 2023/4/30 19:14
 */

@Data
public class UserRegisterRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -543286974259314538L;

    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private String planetCode;
}
