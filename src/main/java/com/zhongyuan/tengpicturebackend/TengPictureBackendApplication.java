package com.zhongyuan.tengpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.zhongyuan.tengpicturebackend.pictureSpace.mapper")
@MapperScan("com.zhongyuan.tengpicturebackend.vip.mapper")
@EnableAsync
public class TengPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TengPictureBackendApplication.class, args);
    }

}
