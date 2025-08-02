package com.api;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication(exclude = {MybatisAutoConfiguration.class})
@MapperScan("com.api.mapper")
public class ApiAutoTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiAutoTestApplication.class, args);
            
    }

}
