package com.conference.registration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 报名服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.conference"})
public class RegistrationApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(RegistrationApplication.class, args);
    }
}
