package com.zhongyuan.tengpicturebackend.common.message;


import lombok.Data;

import java.io.Serializable;

@Data
public class VipOrderMessage implements Serializable {

    /**
     * 用户ID
     */
    Long userId;
    /**
     * 购买会员等级
     */
    int vipLevel;
    /**
     * 订单号
     */
    String orderNo;
}
