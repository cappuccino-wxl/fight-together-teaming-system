package com.example.match.service;

import com.example.match.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author asus
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2023-04-30 13:04:14
*/
public interface UserService extends IService<User> {
    /**
     * @description: 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 检查密码
     * @param planetCode 星球编号
     * @return long
     * @date: 2023/4/30 16:45
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * @description: 用户登录
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @return com.example.match.model.domain.User
     * @date: 2023/4/30 18:32
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * @description: 用户脱敏
     * @param originUser
     * @return com.example.match.model.domain.User
     * @date: 2023/4/30 21:48
     */
    User getSafeUser(User originUser);

    /**
     * @description: 用户注销
     * @param request
     * @return int
     * @date: 2023/5/2 13:53
     */
    int userLogout(HttpServletRequest request);

    /**
     * @description: 根据标签搜索用户
     * @param tagNameList
     * @return java.util.List<com.example.match.model.domain.User>
     * @date: 2023/5/4 21:28
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * @description: 更新用户
     * @param user
     * @return java.lang.Integer
     * @date: 2023/5/7 14:58
     */
    int updateUser(User user, User loginUser);

    /**
     * @description: 获取当前用户信息
     * @param request
     * @return com.example.match.model.domain.User
     * @date: 2023/5/7 15:03
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * @description: 判断是否为管理员
     * @param loginUser
     * @return boolean
     * @date: 2023/5/7 15:11
     */
    boolean isAdmin(User loginUser);

    /**
     * @description: 是否为管理员
     * @param request
     * @return boolean
     * @date: 2023/5/12 11:01
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * @description: 匹配用户
     * @param num
     * @param user
     * @return java.util.List<com.example.match.model.domain.User>
     * @date: 2023/5/12 11:01
     */
    List<User> matchUsers(long num, User user);
}
