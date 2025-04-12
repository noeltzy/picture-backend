package com.zhongyuan.tengpicturebackend.pictureSpace.model.entity;

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

    /**
     * 本次任务消耗的token数量
     */
    private Integer tokensUsed;

    /**
     * VIP等级：0-免费用户 1-VIP1 2-VIP2 3-VIP3
     */
    private Long userVipId;

    /**
     * 任务类型：0-生成图片 1-编辑图片
     */
    private Integer taskType;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * token是否已退还：0-未退还 1-已退还 2-无需退还
     */
    private Integer tokenRefunded;

    /**
     * token退还时间
     */
    private Date refundTime;

    /**
     * 退还备注
     */
    private String refundRemark;
}