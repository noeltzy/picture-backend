package com.zhongyuan.tengpicturebackend.pictureSpace.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.ImageGenTask;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.ImageGenTaskService;
import com.zhongyuan.tengpicturebackend.pictureSpace.mapper.ImageGenTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
* @author Windows11
* @description 针对表【image_gen_task】的数据库操作Service实现
* @createDate 2025-04-01 19:47:24
*/
@Service
@Slf4j
public class ImageGenTaskServiceImpl extends ServiceImpl<ImageGenTaskMapper, ImageGenTask>
    implements ImageGenTaskService{

    /**
     * 任务失败，只允许PENDING OR RUNNING的状态能够 更改
     * @param task 当前任务
     */
    @Override
    public void updateTaskToFailed(ImageGenTask task) {

        final String targetStatus = "FAILED";
        boolean update = lambdaUpdate()
                .eq(ImageGenTask::getTaskId, task.getTaskId())
                .eq(ImageGenTask::getStatus, "RUNNING").or().eq(ImageGenTask::getStatus, "PENDING")
                .set(ImageGenTask::getStatus,targetStatus )
                .update();
        if(!update){
            logStatusUpdateError(task, targetStatus);
        }
    }


    /**
     * 只有 PENDING 能变为 RUNNING 目前的计划是这样的
     * 后续可能有任务重新执行的机制 扩展点
     * @param task 当前任务
     */
    @Override
    public void updateTaskToRunning(ImageGenTask task) {
        //TODO 后续重新执行失败的任务,可以让 FAILED 且没回退Token的任务重新执行
        final String targetStatus = "RUNNING";
        boolean update = lambdaUpdate()
                .eq(ImageGenTask::getTaskId, task.getTaskId())
                .eq(ImageGenTask::getStatus, "PENDING")
                .set(ImageGenTask::getStatus,targetStatus )
                .update();
        if(!update){
            logStatusUpdateError(task, targetStatus);
        }
    }

    @Override
    public void markTokenRefunded(ImageGenTask task) {
        boolean update = lambdaUpdate()
                .eq(ImageGenTask::getTaskId, task.getTaskId())
                .eq(ImageGenTask::getStatus, "FAILED")
                .set(ImageGenTask::getTokenRefunded,1 )
                .set(ImageGenTask::getRefundTime,new Date())
                .update();
        if(!update){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
    }

    private  void logStatusUpdateError(ImageGenTask task, String targetStatus) {
        log.info("任务{}状态更新失败，当前任务状态:{}, 预期更新成为的状态:{}",
                task.getTaskId(), task.getStatus(), targetStatus);
    }
}