package com.zhongyuan.tengpicturebackend;

import com.zhongyuan.tengpicturebackend.api.aliyunai.model.common.CreateTaskResponse;
import com.zhongyuan.tengpicturebackend.api.aliyunai.model.genPicture.GenPictureRequest;
import com.zhongyuan.tengpicturebackend.api.aliyunai.model.genPicture.ImageGenerationResponse;
import com.zhongyuan.tengpicturebackend.api.aliyunai.service.AliYunApiService;
import com.zhongyuan.tengpicturebackend.service.PictureService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class TengPictureBackendApplicationTests {
    @Resource
    AliYunApiService aliYunApiService;

    @Resource
    PictureService pictureService;

    @Test
    void testGenPicture() {
        GenPictureRequest genPictureRequest = new GenPictureRequest();
        GenPictureRequest.Input Input = new GenPictureRequest.Input();
        Input.setPrompt("两个人正在床上玩耍");
        genPictureRequest.setInput(Input);
        CreateTaskResponse genPictureTask = aliYunApiService.createGenPictureTask(genPictureRequest);
        System.out.println(genPictureTask);
    }
    @Test
    void getResult() {
        String rqId ="a2c04acb-33a4-93d9-a094-ec9274c0a617";
        ImageGenerationResponse imageGenerationResponse = aliYunApiService.getGenPictureTaskResult("79e810b1-5216-4d79-80a3-63d32e9b7d70");
        System.out.println(imageGenerationResponse);
    }
}
