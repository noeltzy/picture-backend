package com.zhongyuan.tengpicturebackend.vip.model.entity.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class PayVipOrderRequest implements Serializable {
    String orderNo;
    /**
     * 支付了多少钱
     */
    int amount;
}
