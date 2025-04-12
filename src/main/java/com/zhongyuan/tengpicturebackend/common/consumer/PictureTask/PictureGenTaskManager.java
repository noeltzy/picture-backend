package com.zhongyuan.tengpicturebackend.common.consumer.PictureTask;


import cn.hutool.core.util.ObjUtil;
import com.zhongyuan.tengpicturebackend.common.consumer.PictureTask.service.PictureGenTaskService;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.common.message.PictureAiMessage;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.picture.PictureUploadRequest;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.ImageGenTask;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.User;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.vo.PictureVo;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.ImageGenTaskService;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.PictureService;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.UserService;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;


@Slf4j
@Service
public class PictureGenTaskManager {
    @Resource
    WanxImageModel wanxImageModel;
    @Resource
    ImageGenTaskService imageGenTaskService;
    @Resource
    PictureService pictureService;

    @Resource
    PictureGenTaskService pictureGenTaskService;

    @Resource
    UserService userService;

    public void doGenPictureByText(PictureAiMessage message) {
        //查询任务状态 是用幂等性 执行防止消息重复消费
        // 查询是否有任务 无需事物
        ImageGenTask task = validateAndGetTask(message);
        if (task == null) {
            return;
        }
        try {
            Date now = new Date();
            Response<Image> generate = handleRunTask(message, task);
            //任务成功
            handleTaskSuccess(message, generate, task,now);
        }catch (Exception e) {
            // 用到两个service 防止事物失效，方法没有定义在本类中,这里
            log.error("图片生成任务执行失败,任务ID:{}, 错误信息:{}", task.getTaskId(), e.getMessage());
            //先标记任务失败
            imageGenTaskService.updateTaskToFailed(task);
            // 这里的Try 包裹起来是为了防止如果这里下面两部分出现问题，
            // 不希望消息重新执行，只要前置步骤调用过AI接口，无论什么原因失败
            try {
                // 后面执行 这个是需要两个表，一个是vip记录（补偿） 一个是任务记录（更新为已经补偿）
                // 这里出问题就不希望消息重新执行（开启消费者确认模式）
                pictureGenTaskService.handleFailedGeneration(task);
            } catch (Exception ex) {
                log.error(ex.getMessage());
                log.info("补偿机制失败 taskId:{}",task.getTaskId());
                //TODO 可以再发消息，类似兜底，专门用于处理补偿失败，设置重试，最后实在不行的无敌大兜底处理方案就是实在还没成功，就定时扫描表
            }
        }
    }

    private void handleTaskSuccess(PictureAiMessage message, Response<Image> generate, ImageGenTask task,Date beginTime) {
        ImageGenTask succeedTask = new ImageGenTask();
        succeedTask.setTaskId(message.getTaskId());
        String url = generate.content().url().toString();
        try {
            PictureUploadRequest request = new PictureUploadRequest();
            User user = userService.getById(task.getUserId());
            request.setSpaceId(message.getSpaceId());
            PictureVo pvo= pictureService.uploadPicture(url,request,user);
            succeedTask.setUrl(pvo.getUrl());
            // 代表本地上传成功
            succeedTask.setStatus("SUCCEED_1");
        }catch (BusinessException e){
            log.info(e.getMessage());
            // 如果任务失败,回去展示的URL设置为URL
            succeedTask.setUrl(url);
            // 代表当前结果需要用户下载再上传，本地上传出现问题
            succeedTask.setStatus("SUCCEED_0");
        }

        succeedTask.setStartTime(beginTime);
        succeedTask.setEndTime(new Date());
        imageGenTaskService.updateById(succeedTask);
    }

    @NotNull
    private Response<Image> handleRunTask(PictureAiMessage message, ImageGenTask task) {

        imageGenTaskService.updateTaskToRunning(task);
        Response<Image> generate = wanxImageModel.generate(message.getPrompt());
        log.info("taskId:{} origin prompt :{},new prompt:{}", task.getTaskId(),
                message.getPrompt(),generate.content().revisedPrompt());
        return generate;
    }

    private ImageGenTask validateAndGetTask(PictureAiMessage message) {
        ImageGenTask task = imageGenTaskService.getById(message.getTaskId());
        if (!"PENDING".equals(task.getStatus())) {
            return null;
        }
        User user = userService.getById(task.getUserId());
        if (ObjUtil.isNull(user)) {
            log.info("[Gen Picture],任务错误，用户不存在,ID {}", message.getUserId());
            return null;
        }
        return task;
    }




}
