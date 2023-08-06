package com.example.match.once;

import com.example.match.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class InsertUsers {
    @Resource
    private UserMapper userMapper;

    // fixedDelay = 5000：springboot 启动后每隔5s执行一次
    // fixedRate = Long.MAX_VALUE 隔很久很久之后再执行一次
    // 实现启动后只执行一次，不过不是很优雅
//    @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
//    public void doInsertUsers(){
//        final int insertNum = 1000000;
//        StopWatch stopWatch = new StopWatch(); // 计时工具
//        stopWatch.start();
//        // 模拟插入100w条数据
//        for(int i = 0; i < insertNum; i++){
//            User user = new User();
//            user.setUsername("甲用户");
//            user.setUserAccount("甲账户");
//            user.setAvatarUrl("https://img-home.csdnimg.cn/images/20201124032511.png");
//            user.setTags("");
//            user.setGender(0);
//            user.setUserPassword("123");
//            user.setPhone("1234");
//            user.setEmail("1234");
//            user.setUserStatus(0);
//            user.setUserRole(0);
//            user.setPlanetCode("1234");
//            userMapper.insert(user);
//        }
//        stopWatch.start();
//        System.out.println(stopWatch.getTotalTimeMillis());
//    }

    // 在 Bean（标注了@Resource）里写main函数，这个类不会被纳入Spring容器，不是一个bean，执行会报错，空指针异常
    // userMapper 是空的，因为springboot没加载
//    public static void main(String[] args) {
//        new InsertUsers().doInsertUsers();
//    }
}
