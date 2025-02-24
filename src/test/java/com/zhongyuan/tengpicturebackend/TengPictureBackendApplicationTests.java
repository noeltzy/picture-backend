package com.zhongyuan.tengpicturebackend;

import com.zhongyuan.tengpicturebackend.api.aliyunai.config.AliyunConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class TengPictureBackendApplicationTests {

    @Resource
    AliyunConfig aliyunConfig;
    @Test
    void contextLoads() {
        System.out.println(aliyunConfig.getApiKey());
    }

}
