package com.example.match.job;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.match.model.domain.User;
import com.example.match.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

// 缓存预热任务
@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RedissonClient redissonClient;

    // 重点用户
    private final List<Long> mainUserList = List.of(1L);

    // 每天执行，预热推荐用户
    @Scheduled(cron = "0 31 0 * * *") // crontab 表达式，可以使用在线生成器生成，不用自己记，表示每隔多长时间执行一次
    public void doCacheRecommendUser() {
        // redisson的分布式锁
        RLock lock = redissonClient.getLock("match:precachejob:docache:lock"); // 缓存的key，自定义
        try {
            // 只有一个线程能获取到锁
            // 第一个参数waitTime一定是0（等待时间），每天只执行一次，拿到锁就执行，不等待
            // 第二个参数leaseTime，过期时间，设置为-1才能使用redisson本身的看门狗机制，每隔一段时间开启一个监听线程，如果方法还没执行完就帮你重置redis的过期时间
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                System.out.println("getLock: " + Thread.currentThread().getId());
                for (Long userId : mainUserList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    String redisKey = String.format("match:user:recommend:%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    // 写缓存
                    try {
                        valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                }
                // redisson里提供了续期机制，不需要自己实现过期时间
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
        } finally { // 注意释放锁要放到finally里，否则try部分抛异常就执行不到了
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) { // 当前锁是不是当前线程的，根据线程id
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

}

