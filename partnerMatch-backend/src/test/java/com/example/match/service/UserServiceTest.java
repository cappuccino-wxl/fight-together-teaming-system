package com.example.match.service;
import java.util.Arrays;
import java.util.List;

import com.example.match.model.domain.User;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @Test
    public void testAddUser(){
        User user = new User();
        user.setUsername("kabuqinuo");
        user.setUserAccount("12345678");
        user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
        user.setGender(0);
        user.setUserPassword("12345678");
        user.setPhone("12345678");
        user.setEmail("1740284455@qq.com");
        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

    @Test
    public void userRegister() {
        String userAccount = "yupi";
        String userPassword = "123";
        String checkPassword = "123456";
        String planetCode = "12";
//        long len = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1, len);
//        userAccount = "12";
//        len = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1, len);
//        userAccount = "yupi";
//        checkPassword = "123456";
//        len = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1, len);
//        userAccount = "y upi";
//        checkPassword = "12345678";
//        len = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1, len);
//        userAccount = "qinuo";
//        userPassword = "123456789";
//        checkPassword = "123456789";
//        len = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        System.out.println(len);
//        Assertions.assertEquals(-1, len);
        userAccount = "kabu";
        userPassword = "12345678";
        checkPassword = "12345678";
        long len = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        System.out.println(len);
//        Assertions.assertEquals(-1, len);
    }


    @Test
    public void testSearchUsersByTags(){
        List<String> tagNameList = Arrays.asList("java", "python");
        List<User> userList = userService.searchUsersByTags(tagNameList);
        System.out.println(userList);
//        Assert.assertNotNull(userList);
    }
}