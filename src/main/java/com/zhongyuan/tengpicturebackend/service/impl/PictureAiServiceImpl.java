package com.zhongyuan.tengpicturebackend.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.zhongyuan.tengpicturebackend.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.exception.ThrowUtils;
import com.zhongyuan.tengpicturebackend.message.PictureAiMessage;
import com.zhongyuan.tengpicturebackend.model.entity.Space;
import com.zhongyuan.tengpicturebackend.service.ImageGenTaskService;
import com.zhongyuan.tengpicturebackend.service.PictureAiService;
import com.zhongyuan.tengpicturebackend.model.dto.ai.GenPictureTaskRequest;
import com.zhongyuan.tengpicturebackend.model.entity.ImageGenTask;
import com.zhongyuan.tengpicturebackend.model.entity.User;
import com.zhongyuan.tengpicturebackend.service.SpaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;


@Slf4j
@Service
public class PictureAiServiceImpl implements PictureAiService {

    @Resource
    RabbitTemplate rabbitTemplate;

    @Resource
    ImageGenTaskService imageGenTaskService;

    @Resource
    SpaceService spaceService;

    @Override
    public ImageGenTask createGenPictureTask(GenPictureTaskRequest genPictureTaskRequest, User loginUser) {

        // 参数和权限校验
        String prompt = genPictureTaskRequest.getPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(prompt),ErrorCode.PARAMS_ERROR,"prompt为空");
        Long spaceId = genPictureTaskRequest.getSpaceId();
        Space space = spaceService.lambdaQuery().eq(Space::getId, spaceId).one();
        if (space == null|| !Objects.equals(space.getUserId(), loginUser.getId())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"您无权在他人空间进行次操作");
        }

        //生成任务
        long id = IdUtil.getSnowflake().nextId();
        ImageGenTask genPictureTaskResponse = new ImageGenTask();
        genPictureTaskResponse.setTaskId(id);
        genPictureTaskResponse.setUserId(loginUser.getId());
        // 插入任务记录
        boolean save = imageGenTaskService.save(genPictureTaskResponse);
        if(!save){
            log.info("gen_image_error,prompt:{},userId:{}",prompt,loginUser.getId());
            throw  new BusinessException(ErrorCode.OPERATION_ERROR,"任务执行失败");
        }

        //发送消息到ai扩图消息队列
        PictureAiMessage message = new PictureAiMessage();
        message.setTaskId(id);
        message.setPrompt(prompt);
        message.setSpaceId(spaceId);
        message.setUserId(loginUser.getId());
        message.setTaskType("GenByText");
        // 发送消息
        rabbitTemplate.convertAndSend("picture.ai.gen.topic","picture.ai.gen", message);
        //返回结果
        return genPictureTaskResponse;
    }
}
