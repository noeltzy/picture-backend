package com.zhongyuan.tengpicturebackend.manager.mq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.zhongyuan.tengpicturebackend.manager.mq.constant.MqConstant.UPLOAD_PICTURE_MQ;

@Configuration
public class RabbitMqConfig {
    @Bean
    public Queue uploadPictureQueue() {
        return new Queue(UPLOAD_PICTURE_MQ, false);
    }
}
