package com.zhongyuan.tengpicturebackend.pictureSpace.manager.api.aliyunai.service;

import com.zhongyuan.tengpicturebackend.pictureSpace.manager.api.aliyunai.model.genPicture.GenPictureRequest;
import com.zhongyuan.tengpicturebackend.pictureSpace.manager.api.aliyunai.model.genPicture.ImageGenerationResponse;
import com.zhongyuan.tengpicturebackend.pictureSpace.manager.api.aliyunai.model.outPainting.CreateOutPaintingTaskRequest;
import com.zhongyuan.tengpicturebackend.pictureSpace.manager.api.aliyunai.model.common.CreateTaskResponse;
import com.zhongyuan.tengpicturebackend.pictureSpace.manager.api.aliyunai.model.outPainting.GetOutPaintingTaskResponse;

public interface AliYunApiService {
    CreateTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest request);

    CreateTaskResponse createGenPictureTask(GenPictureRequest request);

    GetOutPaintingTaskResponse getOutPaintingTask(String taskId);
    ImageGenerationResponse getGenPictureTaskResult(String taskId);

} 