package com.example.match.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 配置
 */
@Configuration
// 对应配置文件中redis的配置
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedissonConfig {
    private String host;
    private String port;
    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);
        config.useSingleServer().setAddress(redisAddress).setDatabase(2);
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
