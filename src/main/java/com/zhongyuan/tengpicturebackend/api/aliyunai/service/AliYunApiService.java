package com.zhongyuan.tengpicturebackend.api.aliyunai.service;

import com.zhongyuan.tengpicturebackend.api.aliyunai.model.genPicture.GenPictureRequest;
import com.zhongyuan.tengpicturebackend.api.aliyunai.model.outPainting.CreateOutPaintingTaskRequest;
import com.zhongyuan.tengpicturebackend.api.aliyunai.model.common.CreateTaskResponse;
import com.zhongyuan.tengpicturebackend.api.aliyunai.model.outPainting.GetOutPaintingTaskResponse;

public interface AliYunApiService {
    CreateTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest request);

    CreateTaskResponse createGenPictureTask(GenPictureRequest request);

    GetOutPaintingTaskResponse getOutPaintingTask(String taskId);
} 