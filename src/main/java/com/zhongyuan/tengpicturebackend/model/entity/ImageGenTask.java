package com.zhongyuan.tengpicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName image_gen_task
 */
@TableName(value ="image_gen_task")
@Data
public class ImageGenTask {
    /**
     * 任务ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private Long taskId;

    /**
     * 任务开始时间
     */
    private Date startTime;

    /**
     * 任务结束时间
     */
    private Date endTime;

    /**
     * 任务相关URL
     */
    private String url;

    /**
     * 任务状态（默认：执行中）
     */
    private String status;

    /**
     * 用户ID
     */
    private Long userId;
}