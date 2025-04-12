package com.zhongyuan.tengpicturebackend.manager.api.aliyunai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "aliyun")
public class AliyunConfig {
    private String apiKey;
}
