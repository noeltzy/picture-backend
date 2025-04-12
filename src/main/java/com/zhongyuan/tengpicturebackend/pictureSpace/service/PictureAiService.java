package com.zhongyuan.tengpicturebackend.service;

import com.zhongyuan.tengpicturebackend.model.dto.ai.GenPictureTaskRequest;
import com.zhongyuan.tengpicturebackend.model.entity.ImageGenTask;
import com.zhongyuan.tengpicturebackend.model.entity.User;

public interface PictureAiService {


    ImageGenTask createGenPictureTask(GenPictureTaskRequest genPictureTaskRequest, User loginUser);
}
