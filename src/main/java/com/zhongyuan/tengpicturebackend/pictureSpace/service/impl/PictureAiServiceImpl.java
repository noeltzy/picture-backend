package com.zhongyuan.tengpicturebackend.pictureSpace.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.ThrowUtils;
import com.zhongyuan.tengpicturebackend.common.message.PictureAiMessage;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.Space;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.ImageGenTaskService;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.PictureAiService;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.ai.GenPictureTaskRequest;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.ImageGenTask;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.User;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.SpaceService;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.UserService;
import com.zhongyuan.tengpicturebackend.vip.model.entity.UserVip;
import com.zhongyuan.tengpicturebackend.vip.model.enums.VipLevelEnum;
import com.zhongyuan.tengpicturebackend.vip.service.UserVipService;
import com.zhongyuan.tengpicturebackend.vip.utils.VipUtils;
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

    @Resource
    UserVipService userVipService;

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

        //  查看当前是否有权限执行任务:
        UserVip userVip = userVipService.getByUserId(loginUser.getId());
        int tokenRequired =5;
        boolean tokenEnough = VipUtils.isTokenEnough(VipLevelEnum.FREE, userVip.getUsedTokens(), tokenRequired);
        //  余下额度不够
        ThrowUtils.throwIf(!tokenEnough,ErrorCode.NO_TOKEN_QUOTA_ERROR);
        Long userVipId = userVip.getId();
        //  消耗额度
        userVipService.useTokens(userVipId,tokenRequired);
        //生成任务
        long id = IdUtil.getSnowflake().nextId();
        ImageGenTask genPictureTaskResponse = new ImageGenTask();
        genPictureTaskResponse.setTaskId(id);
        genPictureTaskResponse.setUserId(loginUser.getId());
        genPictureTaskResponse.setTokensUsed(tokenRequired);
        genPictureTaskResponse.setUserVipId(userVipId);
        // 设置次数消耗token是以什么VIP记录消耗的
        // 插入任务记录
        boolean save = imageGenTaskService.save(genPictureTaskResponse);
        if(!save){
            log.info("gen_image_error,prompt:{},userId:{}",prompt,loginUser.getId());
            throw  new BusinessException(ErrorCode.OPERATION_ERROR,"任务启动失败");
        }
        //发送消息到ai扩图消息队列
        PictureAiMessage message = new PictureAiMessage();
        message.setTaskId(id);
        message.setPrompt(prompt);
        message.setSpaceId(spaceId);
        message.setUserId(loginUser.getId());
        //TODO 用枚举类改造
        message.setTaskType(1);
        message.setUserVipId(userVipId);
        // 发送消息
        rabbitTemplate.convertAndSend("picture.ai.gen.topic","picture.ai.gen", message);
        //返回结果
        return genPictureTaskResponse;
    }
}
