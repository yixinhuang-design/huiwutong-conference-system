package com.conference.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 认证授权服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.conference.auth", "com.conference.common"})
@EnableDiscoveryClient
@MapperScan("com.conference.auth.mapper")
public class AuthApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
        System.out.println("============================================");
        System.out.println("    智能会务系统 - 认证授权服务 启动成功！");
        System.out.println("    端口: 8081");
        System.out.println("============================================");
    }
}
