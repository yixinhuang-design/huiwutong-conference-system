package com.conference.seating;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan({"com.conference.seating", "com.conference.common"})
@MapperScan("com.conference.seating.mapper")
public class SeatingApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeatingApplication.class, args);
    }
}

