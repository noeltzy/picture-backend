package com.zhongyuan.tengpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.zhongyuan.tengpicturebackend.mapper")
@EnableAspectJAutoProxy(proxyTargetClass=true)
public class TengPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TengPictureBackendApplication.class, args);
    }

}