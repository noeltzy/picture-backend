package com.zhongyuan.tengpicturebackend;


import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class Aitest {

    @Value("${langchain4j.community.dashscope.chat-model.api-key}")
    private String apiKey;





    @Test
    void contextLoads() {
        ChatLanguageModel chatLanguageModel = QwenChatModel.builder().apiKey(apiKey)
                .modelName("qwen-max")
                .build();
        String chat = chatLanguageModel.chat("介绍你的模型");
        System.out.println(chat);
    }

    @Test
    void image() {
        WanxImageModel wanxImageModel = WanxImageModel.builder().apiKey(apiKey)
                .modelName("wanx2.1-t2i-turbo")
                .build();
        Response<Image> im = wanxImageModel.generate("请你生成一张漂亮的图片");
        System.out.println(im.content());
    }
}
