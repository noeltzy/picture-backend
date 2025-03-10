package com.zhongyuan.tengpicturebackend.manager.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.zhongyuan.tengpicturebackend.manager.mq.constant.MqConstant.UPLOAD_PICTURE_MQ;

@Component
public class PictureUploadProducer {

    @Resource
    RabbitTemplate rabbitTemplate;

    public void sendMessage(PictureUploadMessage message) {
        rabbitTemplate.convertAndSend(UPLOAD_PICTURE_MQ, message);
        System.out.println("发送消息: " + message);
    }
}
