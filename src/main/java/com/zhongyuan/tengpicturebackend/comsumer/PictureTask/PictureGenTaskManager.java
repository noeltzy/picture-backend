package com.zhongyuan.tengpicturebackend.comsumer.PictureTask;


import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.zhongyuan.tengpicturebackend.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.message.PictureAiMessage;
import com.zhongyuan.tengpicturebackend.model.dto.picture.PictureUploadRequest;
import com.zhongyuan.tengpicturebackend.model.entity.ImageGenTask;
import com.zhongyuan.tengpicturebackend.model.entity.User;
import com.zhongyuan.tengpicturebackend.model.vo.PictureVo;
import com.zhongyuan.tengpicturebackend.service.ImageGenTaskService;
import com.zhongyuan.tengpicturebackend.service.PictureService;
import com.zhongyuan.tengpicturebackend.service.UserService;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
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
    UserService userService;

    public void doGenPictureByText(PictureAiMessage message) {
        //查询任务状态 是用幂等性 执行防止消息重复消费
        // 查询是否有任务
        ImageGenTask task = imageGenTaskService.getById(message.getTaskId());
        // 当前任务非排队中,说明已经执行过了
        if(!"PENDING".equals(task.getStatus())) {
            return;
        }

        //校验基本逻辑
        User user = userService.getById(task.getUserId());
        if(ObjUtil.isNull(user)){
            log.info("[Gen Picture],任务错误，用户不存在,ID {}",message.getUserId());
            return;
        }
        // 执行任务
        Date beginTime = new Date();
        try {
            imageGenTaskService.lambdaUpdate()
                    .eq(ImageGenTask::getTaskId, message.getTaskId())
                    .set(ImageGenTask::getStatus,"RUNNING");
            Response<Image> generate = wanxImageModel.generate(message.getPrompt());

            log.info("taskId:{} origin prompt :{},new prompt:{}",task.getTaskId(),
                    message.getPrompt(),generate.content().revisedPrompt());

            ImageGenTask succeedTask = new ImageGenTask();
            succeedTask.setTaskId(message.getTaskId());
            String url = generate.content().url().toString();
            try {
                PictureUploadRequest request = new PictureUploadRequest();
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

        }catch (Exception e) {
            System.out.println(e.getMessage());
            // TODO 任务执行失败，可以给前面扣减的 调用次数恢复过来
            log.info("图片生成任务执行失败,任务详细信息:{}",JSONUtil.toJsonStr(message));
            ImageGenTask failTask = new ImageGenTask();
            failTask.setTaskId(message.getTaskId());
            failTask.setStatus("FAILED");
            failTask.setStartTime(beginTime);
            failTask.setEndTime(new Date());
            imageGenTaskService.updateById(failTask);
        }
    }
}
