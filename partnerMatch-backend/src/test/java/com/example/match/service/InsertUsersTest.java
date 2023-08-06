package com.example.match.service;

import com.example.match.model.domain.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
public class InsertUsersTest {
    @Resource
    private UserService userService;

    // 面试常问
    // CPU 密集型：分配的核心线程数 = CPU - 1
    // IO 密集型：分配的核心线程数可以大于CPU核数
    private ExecutorService executorService = new ThreadPoolExecutor(60, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    /**
     * @description: 批量插入用户，使用mybatis的方法
     * 运行时间：21565
     * @date: 2023/5/8 12:34
     */
    @Test
    public void doInsertUsers(){
        final int insertNum = 100000;
        StopWatch stopWatch = new StopWatch(); // 计时工具
        stopWatch.start();
        List<User> userList = new ArrayList<>();
        // 模拟插入10w条数据
        for(int i = 0; i < insertNum; i++){
            User user = new User();
            user.setUsername("kabuqinuo");
            user.setUserAccount("naicha");
            user.setAvatarUrl("https://img-home.csdnimg.cn/images/20201124032511.png");
            user.setTags("");
            user.setGender(1);
            user.setUserPassword("124563");
            user.setPhone("2625265");
            user.setEmail("4256626");
            user.setUserStatus(1);
            user.setUserRole(1);
            user.setPlanetCode("4658578");
            userList.add(user);
        }

        userService.saveBatch(userList, 10000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }


    /**
     * @description: 并发批量插入数据
     * 运行时间：9916，可以多测试测试
     * @date: 2023/5/8 12:34
     */
    @Test
    public void doConcurrencyInsertUsers(){
        final int insertNum = 100000;
        StopWatch stopWatch = new StopWatch(); // 计时工具
        stopWatch.start();
        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        // 模拟插入100w条数据
        for(int i = 0; i < 10; i++){
            // 用线程安全的集合
            List<User> userList = Collections.synchronizedList(new ArrayList<>());
            while(true){
                j++; // 不是原子操作，不能放到并发里
                User user = new User();
                user.setUsername("卡布奇诺");
                user.setUserAccount("Cappuccino");
                user.setAvatarUrl("https://img-home.csdnimg.cn/images/20201124032511.png");
                user.setTags("['java','python', 'html']");
                user.setGender(0);
                user.setUserPassword("123567");
                user.setPhone("123415656");
                user.setEmail("12313454");
                user.setUserStatus(0);
                user.setUserRole(1);
                user.setPlanetCode("21066");
                userList.add(user);
                if(j % 10000 == 0){
                    break;
                }
            }
            // runAsync 异步，用的java默认的线程池ForkJoinPool
            // 会有多个线程干两份活
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println(Thread.currentThread().getName());
                userService.saveBatch(userList, 10000);
            }, executorService); // 定义自己的线程池
            futureList.add(future);
        }
        // join 异步
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}


