package com.zhongyuan.tengpicturebackend.common.consumer.picture;


import com.zhongyuan.tengpicturebackend.common.consumer.PictureTask.PictureGenTaskManager;
import com.zhongyuan.tengpicturebackend.common.message.PictureAiMessage;
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
        int taskType = message.getTaskType();
        switch (taskType) {
            case 1:
                pictureGenTaskManager.doGenPictureByText(message);
                break;
            default:
                log.info("error,taskType:{}", taskType);
        }
    }
}
