package com.zhongyuan.tengpicturebackend.controller;


import cn.hutool.core.util.StrUtil;
import com.zhongyuan.tengpicturebackend.annotation.RequestLimit;
import com.zhongyuan.tengpicturebackend.api.aliyunai.model.common.CreateTaskResponse;
import com.zhongyuan.tengpicturebackend.api.aliyunai.model.genPicture.GenPictureRequest;
import com.zhongyuan.tengpicturebackend.api.aliyunai.model.genPicture.ImageGenerationResponse;
import com.zhongyuan.tengpicturebackend.api.aliyunai.model.outPainting.GetOutPaintingTaskResponse;
import com.zhongyuan.tengpicturebackend.common.BaseResponse;
import com.zhongyuan.tengpicturebackend.common.ResultUtils;
import com.zhongyuan.tengpicturebackend.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.exception.ThrowUtils;
import com.zhongyuan.tengpicturebackend.model.dto.picture.CreatePictureOutPaintingTaskRequest;
import com.zhongyuan.tengpicturebackend.model.entity.User;
import com.zhongyuan.tengpicturebackend.service.PictureService;
import com.zhongyuan.tengpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/ai/picture")
@Slf4j
public class AIController {


    @Resource
    UserService userService;

    @Resource
    PictureService pictureService;
    /**
     * 创建 AI 扩图任务
     */
    @RequestLimit(key = "genPicture",times = 1,duration = 10)
    @PostMapping("/out_painting/create_task")
    public BaseResponse<CreateTaskResponse> createPictureOutPaintingTask(
            @RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
            HttpServletRequest request) {
        if (createPictureOutPaintingTaskRequest == null || createPictureOutPaintingTaskRequest.getPictureId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        CreateTaskResponse response = pictureService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, loginUser);
        return ResultUtils.success(response);
    }
    /**
     * 查询 AI 扩图任务
     */
    @GetMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getPictureOutPaintingTask(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        GetOutPaintingTaskResponse task = pictureService.getOutPaintingResult(taskId);
        if("SUCCEEDED".equals(task.getOutput().getTaskStatus())){
            String imgUrl = task.getOutput().getOutputImageUrl();

        }
        return ResultUtils.success(task);
    }

    /**
     * 创建图像生成任务
     * @param genPictureRequest
     * @param request
     * @return
     */

    @RequestLimit(key = "genPicture",times = 1,duration = 10)
    @PostMapping("/gen_picture/create_task")
    public BaseResponse<CreateTaskResponse> genPictureCreateTask(
            @RequestBody GenPictureRequest genPictureRequest,
            HttpServletRequest request) {
        if (genPictureRequest == null ) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        CreateTaskResponse response = pictureService.createGenPictureTask(genPictureRequest, loginUser);
        return ResultUtils.success(response);
    }

    @GetMapping("/gen_picture/get_task")
    public BaseResponse<ImageGenerationResponse> getGenPictureTask(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        ImageGenerationResponse generationResult = pictureService.getGenerationResult(taskId);
        return ResultUtils.success(generationResult);
    }
}
