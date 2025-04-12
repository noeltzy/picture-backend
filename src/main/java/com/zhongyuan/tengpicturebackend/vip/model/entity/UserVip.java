package com.zhongyuan.tengpicturebackend.vip.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 用户VIP表
 * @TableName user_vip
 */
@TableName(value ="user_vip")
@Data
public class UserVip {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * VIP等级：0-免费用户 1-VIP1 2-VIP2 3-VIP3
     */
    private Integer vipLevel;

    /**
     * Token总额度
     */
    private Long totalTokens;

    /**
     * 已使用Token数量
     */
    private Long usedTokens;

    /**
     * 每日可调用次数
     */
    private Integer dailyLimit;

    /**
     * 生效时间
     */
    private Date startTime;

    /**
     * 到期时间
     */
    private Date endTime;

    /**
     * 状态：0-已失效 1-生效中 2-已过期
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 订单号(固定20位)
     */
    private String orderNo;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;
}