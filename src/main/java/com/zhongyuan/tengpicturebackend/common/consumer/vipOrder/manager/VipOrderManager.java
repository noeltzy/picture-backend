package com.zhongyuan.tengpicturebackend.common.consumer.vipOrder.manager;


import com.zhongyuan.tengpicturebackend.common.message.VipOrderMessage;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.User;
import com.zhongyuan.tengpicturebackend.vip.model.entity.UserVip;
import com.zhongyuan.tengpicturebackend.vip.service.UserVipService;
import com.zhongyuan.tengpicturebackend.vip.service.VipOrderService;
import com.zhongyuan.tengpicturebackend.vip.utils.VipUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Component
public class VipOrderManager {


    @Resource
    private UserVipService userVipService;

    @Resource
    private VipOrderService vipOrderService;

    @Transactional
    public boolean openVip(User user, VipOrderMessage message) {
        // 1. 检查用户是否有VIP
        UserVip oldVip = userVipService.getByUserId(message.getUserId());
        // 2. 如果有VIP且VIP等级比目标VIP等级低
        if (!VipUtils.canUpgrade(oldVip.getVipLevel(), message.getVipLevel())) {
            log.info("用户{}的VIP等级{}比目标VIP等级{}高或者相等，无法升级", user.getUserAccount(), oldVip.getVipLevel(), message.getVipLevel());
            return false;
        }
        UserVip newVip = VipUtils.createUserVip(user.getId(), VipUtils.getByLevel(message.getVipLevel()), oldVip, message.getOrderNo());
        // 前置vip失效:
        userVipService.cancelVip(oldVip);
        userVipService.save(newVip);
        //订单状态设置为处理完成
        vipOrderService.consumeVipOrder(message.getOrderNo());
        return true;
    }
}
