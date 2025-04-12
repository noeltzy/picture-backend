package com.zhongyuan.tengpicturebackend.vip.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * VIP订单表
 * @TableName vip_order
 */
@TableName(value ="vip_order")
@Data
public class VipOrder {
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
     * 订单号(固定20位)
     */
    private String orderNo;

    /**
     * VIP等级
     */
    private Integer vipLevel;

    /**
     * 支付金额(单位:分)
     */
    private Integer amount;

    /**
     * 支付状态：0-未支付 1-已支付 2-已取消
     */
    private Integer payStatus;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;
}