package com.rollcall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 课堂点名系统启动类
 */
@SpringBootApplication
@MapperScan("com.rollcall.mapper")
public class RollCallApplication {

    public static void main(String[] args) {
        SpringApplication.run(RollCallApplication.class, args);
    }
}
