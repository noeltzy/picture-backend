package com.zhongyuan.tengpicturebackend.comsumer.picture;


import com.zhongyuan.tengpicturebackend.comsumer.PictureTask.PictureGenTaskManager;
import com.zhongyuan.tengpicturebackend.message.PictureAiMessage;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class PictureAiGenConsumer {

    @Resource
    PictureGenTaskManager pictureGenTaskManager;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "picture.ai.gen.queue", durable = "true"),
            exchange = @Exchange(name = "picture.ai.gen.topic", type = ExchangeTypes.TOPIC),
            key = "picture.ai.gen"
    ))
    public void listenPaySuccess(PictureAiMessage message) {
        String taskType = message.getTaskType();
        if("GenByText".equals(taskType)) {
            pictureGenTaskManager.doGenPictureByText(message);
        }
    }
}
