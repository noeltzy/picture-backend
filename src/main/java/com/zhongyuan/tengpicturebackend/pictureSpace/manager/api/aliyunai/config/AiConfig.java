package com.zhongyuan.tengpicturebackend.manager.api.aliyunai.config;


import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {


    @Value("${langchain4j.community.dashscope.chat-model.api-key}")
    private String apiKey;

    @Bean
    public QwenChatModel qwenMaxChatModel() {
        return  QwenChatModel.builder().apiKey(apiKey)
                .modelName("qwen-max")
                .build();
    }

    @Bean
    public WanxImageModel wanxMaxImageModel() {
        return  WanxImageModel.builder().apiKey(apiKey)
                .modelName("wanx2.1-t2i-turbo")
                .build();
    }
}
