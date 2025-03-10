package com.zhongyuan.tengpicturebackend.manager.mq;

import com.zhongyuan.tengpicturebackend.model.vo.PictureVo;
import org.springframework.web.multipart.MultipartFile;
import com.zhongyuan.tengpicturebackend.model.entity.User;
import com.zhongyuan.tengpicturebackend.model.dto.picture.PictureUploadRequest;

import com.zhongyuan.tengpicturebackend.service.PictureService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.zhongyuan.tengpicturebackend.manager.mq.constant.MqConstant.UPLOAD_PICTURE_MQ;

@Service
public class PictureUploadConsumer {
    @Resource
    PictureService pictureService;

    @RabbitListener(queues = UPLOAD_PICTURE_MQ)
    public void receiveMessage(PictureUploadMessage message) {
        PictureUploadRequest pictureUploadRequest = message.getPictureUploadRequest();
        MultipartFile file = message.getFile();
        User loginUser = message.getLoginUser();
        PictureVo pictureVo = pictureService.uploadPicture(file, pictureUploadRequest, loginUser);
        //TODO SSE
        System.out.println(pictureVo.getId());
    }
}