package com.zhongyuan.tengpicturebackend.vip.service;

import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.User;
import com.zhongyuan.tengpicturebackend.vip.model.entity.VipOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhongyuan.tengpicturebackend.vip.model.entity.dto.CreateVipOrderRequest;
import com.zhongyuan.tengpicturebackend.vip.model.entity.dto.PayVipOrderRequest;

/**
* @author Windows11
* @description 针对表【vip_order(VIP订单表)】的数据库操作Service
* @createDate 2025-04-02 20:37:11
*/
public interface VipOrderService extends IService<VipOrder> {
    VipOrder createBuyVipOrder(CreateVipOrderRequest createVipOrderRequest, User loginUser);

    String payBuyVipOrder(PayVipOrderRequest payVipOrderRequest, User loginUser);

    VipOrder getVipOrderByOrderNo(String orderNo);

    void consumeVipOrder(String orderNo);
}
