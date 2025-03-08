package com.zhongyuan.tengpicturebackend.api.aliyunai.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.zhongyuan.tengpicturebackend.api.aliyunai.config.AliyunConfig;
import com.zhongyuan.tengpicturebackend.api.aliyunai.model.common.ApiRequest;
import com.zhongyuan.tengpicturebackend.api.aliyunai.model.genPicture.ImageGenerationResponse;
import com.zhongyuan.tengpicturebackend.api.aliyunai.model.outPainting.CreateOutPaintingTaskRequest;
import com.zhongyuan.tengpicturebackend.api.aliyunai.model.common.CreateTaskResponse;
import com.zhongyuan.tengpicturebackend.api.aliyunai.model.outPainting.GetOutPaintingTaskResponse;
import com.zhongyuan.tengpicturebackend.api.aliyunai.model.genPicture.GenPictureRequest;
import com.zhongyuan.tengpicturebackend.api.aliyunai.service.AliYunApiService;
import com.zhongyuan.tengpicturebackend.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class AliYunApiServiceImpl implements AliYunApiService {

    @Resource
    private AliyunConfig aliyunConfig;

    private static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";
    private static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";
    private static final String CREATE_GEN_PICTURE_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis";

    @Override
    public CreateTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest request) {
        return sendRequest(CREATE_OUT_PAINTING_TASK_URL, request, CreateTaskResponse.class);
    }

    @Override
    public CreateTaskResponse createGenPictureTask(GenPictureRequest request) {
        return sendRequest(CREATE_GEN_PICTURE_TASK_URL, request, CreateTaskResponse.class);
    }

    @Override
    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {
        String apiKey = aliyunConfig.getApiKey();
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "任务 id 不能为空");
        }
        try (HttpResponse httpResponse = HttpRequest.get(String.format(GET_OUT_PAINTING_TASK_URL, taskId))
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                .execute()) {
            if (!httpResponse.isOk()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取任务失败");
            }
            return JSONUtil.toBean(httpResponse.body(), GetOutPaintingTaskResponse.class);
        }
    }

    @Override
    public ImageGenerationResponse getGenPictureTaskResult(String taskId) {
        String apiKey = aliyunConfig.getApiKey();
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "任务 id 不能为空");
        }
        try (HttpResponse httpResponse = HttpRequest.get(String.format(GET_OUT_PAINTING_TASK_URL, taskId))
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                .execute()) {
            if (!httpResponse.isOk()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取任务失败");
            }
            return JSONUtil.toBean(httpResponse.body(), ImageGenerationResponse.class);

        }
    }

    public <T extends ApiRequest, R> R sendRequest(String url, T request, Class<R> responseType) {
        String apiKey = aliyunConfig.getApiKey();
        if (request == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请求参数为空");
        }
        
        // 发送请求 
        HttpRequest httpRequest = HttpRequest.post(url)
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                .header("X-DashScope-Async", "enable")
                .header(Header.CONTENT_TYPE, ContentType.JSON.getValue())
                .body(JSONUtil.toJsonStr(request));
        
        try (HttpResponse httpResponse = httpRequest.execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求异常：{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "请求失败");
            }
            return JSONUtil.toBean(httpResponse.body(), responseType);
        }
    }
} 