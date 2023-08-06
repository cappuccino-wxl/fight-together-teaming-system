package com.example.match;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @description: springboot启动类
 * @date: 2023/5/12 10:57
 */
@SpringBootApplication
@MapperScan("com.example.match.mapper") // MyBatis用于扫描Mapper接口
@EnableScheduling // 在 springboot 中开启定时任务
public class PartnerMatchApplication {
	public static void main(String[] args) {
		SpringApplication.run(PartnerMatchApplication.class, args);
	}

}
