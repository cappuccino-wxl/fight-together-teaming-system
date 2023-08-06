package com.example.match.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.match.common.ErrorCode;
import com.example.match.constant.UserConstant;
import com.example.match.exception.BusinessException;
import com.example.match.model.domain.User;
import com.example.match.service.UserService;
import com.example.match.mapper.UserMapper;
import com.example.match.utils.AlgorithmUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import jakarta.servlet.http.HttpServletRequest;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.match.constant.UserConstant.ADMIN_ROLE;
import static com.example.match.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author asus
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2023-04-30 13:04:14
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{
    @Resource
    private UserMapper userMapper;
    // 盐值，混淆密码
    private static final String SALT = "shall";

    /**
     * @description: 用户注册
     * @param userAccount 用户账号
     * @param userPassword 密码
     * @param checkPassword 确认密码
     * @param planetCode 星球编号
     * @return long 用户id
     * @date: 2023/5/2 14:11
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // 校验
        // 1. 任一参数不为空
        if(StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 2. 账号长度>=4
        if(userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        // 3. 密码和确认密码长度>=8
        if(userPassword.length() < 8 || checkPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 4. 星球编号长度<=5
        if(planetCode.length() > 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }
        // 5. 账号不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码包含特殊字符");
        }
        // 6. 密码确认正确
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码确认错误");
        }
        // 7. 账户不能重复
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(wrapper);
        if(count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 8. 星球编号不能重复
        wrapper = new QueryWrapper<>();
        wrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(wrapper);
        if(count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");
        }
        // 密码加密，使用md5并加盐
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 插入数据到数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if(!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }
        return user.getId();
    }

    /**
     * @description: 用户登录
     * @param userAccount 账号
     * @param userPassword 密码
     * @param request http请求
     * @return com.example.match.model.domain.User 用户信息
     * @date: 2023/5/2 14:11
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 校验
        // 1. 参数不为空
        if(StringUtils.isAnyBlank(userAccount, userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 2. 账号长度>=4
        if(userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        // 3. 密码长度>=8
        if(userPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 4. 账号不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符");
        }
        // 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("userAccount", userAccount);
        wrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(wrapper);
        // 用户不存在
        if(user == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在");
        }
        // 用户脱敏
        User safeUser = getSafeUser(user);
        // 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safeUser);
        return safeUser;
    }

    /**
     * @description: 用户脱敏
     * @param originUser 待脱敏用户信息
     * @return com.example.match.model.domain.User
     * @date: 2023/5/2 14:11
     */
    @Override
    public User getSafeUser(User originUser){
        if(originUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在");
        }
        User safeUser = new User();
        safeUser.setId(originUser.getId());
        safeUser.setUsername(originUser.getUsername());
        safeUser.setUserAccount(originUser.getUserAccount());
        safeUser.setAvatarUrl(originUser.getAvatarUrl());
        safeUser.setGender(originUser.getGender());
        safeUser.setPhone(originUser.getPhone());
        safeUser.setEmail(originUser.getEmail());
        safeUser.setPlanetCode(originUser.getPlanetCode());
        safeUser.setUserStatus(originUser.getUserStatus());
        safeUser.setCreateTime(originUser.getCreateTime());
        safeUser.setUserRole(originUser.getUserRole());
        safeUser.setTags(originUser.getTags());
        return safeUser;
    }

    /**
     * @description: 用户注销
     * @param request
     * @return int
     * @date: 2023/5/2 14:14
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    // Tips: 这里测试sql查询和内存查询的时间
    // 交换顺序会有影响，因为第一次数据库连接要花时间，缓存也要花时间（好像没有？）
    // 最终结果：数据量小时，sql查询快一点
    // 建议多加数据子再测一下
    // 内存慢，如何优化：并发，
    // sql查询慢，如何优化：并发，
    // 有时内存快，有时sql快，如何优化：谁先返回用谁；根据数据数量选择用谁，分析什么情况下快
    // 如果参数可以分析，根据参数选择查询方式，比如标签数
    // 如果参数不可分析，并且数据库连接足够，内存足够，同时并发查询，谁先返回用谁
    // sql和内存相结合，先用sql过滤部分tag，后用内存
    // 跟引擎也有关系，多测试
    /**
     * @description: 根据标签搜索用户，内存查询版
     * @param tagNameList 标签列表
     * @return java.util.List<com.example.match.model.domain.User>
     * @date: 2023/5/5 10:00
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签列表为空");
        }
        // 查询所有用户
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(wrapper);
        Gson gson = new Gson();
        // 在内存中判断是否包含要查询的标签
        // 把stream换成parallelStream就可以实现并发，不过有缺陷，尽量不用，看情况，公共线程池
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            if(StringUtils.isBlank(tagsStr)){
                return false;
            }
            // 将 json 数组解析成 string 类型的 set 集合
            Set<String> tagSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {}.getType());
            // 如果 tagSet 是空，赋值为 HashSet 对象，否则不进行任何操作，确保 tagSet 不会为 null
            tagSet = Optional.ofNullable(tagSet).orElse(new HashSet<>());
            // tagNameList 为要查询的标签列表, tagSet 为数据库中所有的标签列表
            for (String tag : tagNameList) {
                // 遍历要查询的标签列表，只要有一个不在数据库中，返回false
                if(!tagSet.contains(tag)){
                    return false;
                }
            }
            return true;
        }).map(this::getSafeUser).collect(Collectors.toList());
    }

    /**
     * @description: 更新用户信息，只有当前登录用户和管理员可以更新
     * @param user 要更新的用户信息
     * @param loginUser 当前登录用户信息
     * @return int
     * @date: 2023/5/7 15:16
     */
    @Override
    public int updateUser(User user, User loginUser) {
        // 查询要更新的用户id
        long id = user.getId();
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id不存在");
        }
        // 如果不是管理员并且不是当前登录用户
        if(!isAdmin(user) && id != loginUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 查询待更新的用户id的用户是否为空
        User oldUser = userMapper.selectById(id);
        if(oldUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户id不存在");
        }
        return userMapper.updateById(user);
    }

    /**
     * @description: 获取当前用户
     * @param request
     * @return com.example.match.model.domain.User
     * @date: 2023/5/7 15:04
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取用户登录态
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(userObj == null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User)userObj;
    }

    /**
     * @description: 当前登录用户是否为管理员
     * @param loginUser 当前登录用户
     * @return boolean
     * @date: 2023/5/12 11:04
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    /**
     * @description: 获取登录态并判断是否为管理员
     * @param request
     * @return boolean
     * @date: 2023/5/12 11:05
     */
    @Override
    public boolean isAdmin(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User)userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * @description: 用户匹配
     * @param num 当前
     * @param loginUser
     * @return java.util.List<com.example.match.model.domain.User>
     * @date: 2023/5/12 11:06
     */
    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 选出id和tags两列，节省查询时间
        queryWrapper.select("id", "tags");
        // 选出非空的tags
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        // 获取当前登录用户的标签
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        // 将tags字符串反序列化为一个List<String>对象，并将其赋值给tagList变量，TypeToken<List<String>>() {}是用于指定反序列化类型的一种方式
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>(){}.getType());
        // 用户列表的下标 => 相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度
        for (User user : userList) {
            String userTags = user.getTags();
            // 无标签或者为当前用户自己
            if (StringUtils.isBlank(userTags) || user.getId().equals(loginUser.getId())) {
                continue;
            }
            // 解析标签
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
        // 按编辑距离由小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .toList();
        // 原本顺序的 userId 列表
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        // 1, 3, 2
        // User1、User2、User3
        // 1 => User1, 2 => User2, 3 => User3
        // 根据用户id分组
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(this::getSafeUser)
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    /**
     * @description: 根据标签搜索用户，sql查询版
     * @param tagNameList
     * @return java.util.List<com.example.match.model.domain.User>
     * @date: 2023/5/5 9:59
     */
    @Deprecated
    private List<User> searchUsersByTagsBySql(List<String> tagNameList) {
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // sql模糊查询
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        for (String tagName : tagNameList) {
            wrapper = wrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(wrapper);
        return userList.stream().map(this::getSafeUser).collect(Collectors.toList());
    }
}




