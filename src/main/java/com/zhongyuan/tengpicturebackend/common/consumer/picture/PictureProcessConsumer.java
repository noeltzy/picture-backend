package com.zhongyuan.tengpicturebackend.common.consumer.picture;


import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.CIUploadResult;
import com.zhongyuan.tengpicturebackend.common.config.CosConfig;
import com.zhongyuan.tengpicturebackend.pictureSpace.manager.cos.CosManager;
import com.zhongyuan.tengpicturebackend.pictureSpace.manager.cos.process.PictureCompress;
import com.zhongyuan.tengpicturebackend.common.message.PictureProcessMessage;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.Picture;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class PictureProcessConsumer {
    @Resource
    public PictureService pictureService;
    @Resource
    private PictureCompress pictureCompress;

    @Resource
    private CosManager cosManager;
    @Resource
    private CosConfig cosConfig;

    public static String extractPath(String url) {
        Pattern pattern = Pattern.compile("myqcloud\\.com/(.+)");
        Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "picture.process.queue", durable = "true"),
            exchange = @Exchange(name = "picture.process.topic", type = ExchangeTypes.TOPIC),
            key = "picture.process"
    ))
    public void listenPaySuccess(PictureProcessMessage message) {
        long pictureId = message.getPictureId();
        String originPictureKey = extractPath(message.getOriginPictureKey());
        message.setOriginPictureKey(originPictureKey);
        // 检查图片是否存在
        Picture oldPicture = pictureService.getById(pictureId);
        if (oldPicture == null) {
            log.info("[process-image-error-not-find-local] pictureId={},key={}", pictureId, originPictureKey);
            return;
        }
        boolean haveKey=cosManager.checkObject(message.getOriginPictureKey());
        if(!haveKey) {
            log.info("[process-image-error-not-find-remote] pictureId={},key={}", pictureId,originPictureKey);
            return;
        }
        List<CIObject> objectList;
        // 远程处理
        try {
            CIUploadResult ciUploadResult = pictureCompress.processPicture(message);
            objectList = ciUploadResult.getProcessResults().getObjectList();
        } catch (Exception e) {
            log.info("[process-image-error-system-remote] pictureId={},key={}", pictureId, originPictureKey);
            return;
        }
        // 修改本地数据库
        Picture picture = new Picture();
        picture.setId(pictureId);
        picture.setUrl(cosConfig.getHost() + "/" + objectList.get(0).getKey());
        if (objectList.size() > 1) {
            picture.setThumbnailUrl(cosConfig.getHost() + "/" + objectList.get(1).getKey());
        }
        boolean update = pictureService.updateById(picture);
        if (!update) {
            log.info("[process-image-error-system-local-update] pictureId={},key={}", pictureId, originPictureKey);
        }
        log.info("[process-image-success] pictureId={},key={}", pictureId, originPictureKey);
    }
}
