package com.zhongyuan.tengpicturebackend.pictureSpace.service;

import com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.ai.GenPictureTaskRequest;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.ImageGenTask;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.User;

public interface PictureAiService {
    ImageGenTask createGenPictureTask(GenPictureTaskRequest genPictureTaskRequest, User loginUser);
}
