package com.zhongyuan.tengpicturebackend.common.consumer.vipOrder;


import cn.hutool.core.util.ObjUtil;
import com.zhongyuan.tengpicturebackend.common.consumer.vipOrder.manager.VipOrderManager;
import com.zhongyuan.tengpicturebackend.common.message.VipOrderMessage;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.User;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.UserService;
import com.zhongyuan.tengpicturebackend.vip.model.entity.VipOrder;
import com.zhongyuan.tengpicturebackend.vip.service.VipOrderService;
import com.zhongyuan.tengpicturebackend.vip.utils.VipOrderUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class vipOrderConsumer {
    @Resource
    private UserService userService;

    @Resource
    private VipOrderService vipOrderService;

    @Resource
    private VipOrderManager vipOrderManager;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "vip.order.queue", durable = "true"),
            exchange = @Exchange(name = "vip.order.topic", type = ExchangeTypes.TOPIC),
            key = "vip.order.buy"
    ))
    public void listenPaySuccess(VipOrderMessage message) {
        Long userId = message.getUserId();
        String orderNo = message.getOrderNo();
        //查询用户
        User orderUser = userService.getById(userId);
        VipOrder vipOrder = vipOrderService.getVipOrderByOrderNo(orderNo);

        // 消费订单幂等性和基础校验
        if(ObjUtil.isNull(orderUser)){
            log.info("[VIP]用户不存在,ID {}", userId);
            return;
        }
        if(ObjUtil.isNull(vipOrder)){
            log.info("[VIP]订单不存在,ID {}", orderNo);
            return;
        }
        if(vipOrder.getPayStatus() == VipOrderUtils.PAY_STATUS_CONSUMED){
            log.info("[VIP]订单已消费，请勿重新消费,ID {}", orderNo);
            return;
        }

        if(vipOrder.getPayStatus() != VipOrderUtils.PAY_STATUS_PAID){
            log.info("[VIP]订单未支付, 消费订单 ID {}", orderNo);
            return;
        }
        boolean result = false;
        //传递message 执行开通逻辑
        try {
            result= vipOrderManager.openVip(orderUser, message);
        } catch (Exception e) {
            // TODO 可以尝试重试 或者记录日志
            log.error("[VIP]订单{}处理失败：{}", orderNo, e.getMessage());
        }
        log.info("[VIP]订单{}处理结果：{}", orderNo, result);

    }
}
