package com.conference.meeting;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.conference.meeting",
    "com.conference.common"
})
@EnableDiscoveryClient
@MapperScan("com.conference.meeting.mapper")
public class ConferenceMeetingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConferenceMeetingApplication.class, args);
    }
}
