package com.zhongyuan.tengpicturebackend.common.comsumer.PictureTask.service;

import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.ImageGenTask;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.ImageGenTaskService;
import com.zhongyuan.tengpicturebackend.vip.service.UserVipService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Slf4j
public class PictureGenTaskService {

    @Resource
    UserVipService userVipService;
    @Resource
    ImageGenTaskService imageGenTaskService;

    /**
     * 能抛出异常
     * @param task
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleFailedGeneration(ImageGenTask task) {

        // 1. 退还token
        // 能抛异常
        userVipService.tokenRefund(task);
        // 能抛出异常
        imageGenTaskService.markTokenRefunded(task);
    }
}
