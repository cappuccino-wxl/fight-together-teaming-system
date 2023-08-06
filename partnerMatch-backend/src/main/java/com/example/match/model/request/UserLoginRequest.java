package com.example.match.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 用户注册请求体
 * @date: 2023/4/30 19:14
 */

@Data
public class UserLoginRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -9177404376080570221L;
    private String userAccount;
    private String userPassword;
}
