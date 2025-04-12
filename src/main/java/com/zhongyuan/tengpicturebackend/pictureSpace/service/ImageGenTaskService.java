package com.zhongyuan.tengpicturebackend.pictureSpace.service;

import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.ImageGenTask;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Windows11
* @description 针对表【image_gen_task】的数据库操作Service
* @createDate 2025-04-01 19:47:24
*/
public interface ImageGenTaskService extends IService<ImageGenTask> {

    void updateTaskToFailed(ImageGenTask task);
    void updateTaskToRunning(ImageGenTask task);

    void markTokenRefunded(ImageGenTask task);
}
