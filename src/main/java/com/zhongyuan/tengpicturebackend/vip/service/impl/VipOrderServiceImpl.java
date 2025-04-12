package com.zhongyuan.tengpicturebackend.vip.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongyuan.tengpicturebackend.common.message.VipOrderMessage;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.User;
import com.zhongyuan.tengpicturebackend.vip.mapper.VipOrderMapper;
import com.zhongyuan.tengpicturebackend.vip.model.entity.VipOrder;
import com.zhongyuan.tengpicturebackend.vip.model.entity.dto.CreateVipOrderRequest;
import com.zhongyuan.tengpicturebackend.vip.model.entity.dto.PayVipOrderRequest;
import com.zhongyuan.tengpicturebackend.vip.model.enums.VipLevelEnum;
import com.zhongyuan.tengpicturebackend.vip.service.UserVipService;
import com.zhongyuan.tengpicturebackend.vip.service.VipOrderService;
import com.zhongyuan.tengpicturebackend.vip.utils.VipOrderUtils;
import com.zhongyuan.tengpicturebackend.vip.utils.VipUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Windows11
 * @description 针对表【vip_order(VIP订单表)】的数据库操作Service实现
 * @createDate 2025-04-02 20:37:11
 */
@Service
public class VipOrderServiceImpl extends ServiceImpl<VipOrderMapper, VipOrder>
        implements VipOrderService {

    @Resource
    private UserVipService userVipService;


    @Resource
    RabbitTemplate rabbitTemplate;
    // 创建订单：
    //
    @Override
    public VipOrder createBuyVipOrder(CreateVipOrderRequest createVipOrderRequest, User loginUser) {
        int currentVipLevel = userVipService.getByUserId(loginUser.getId()).getVipLevel();
        VipLevelEnum currentVipLevelEnum = VipUtils.getByLevel(currentVipLevel);
        VipLevelEnum targetVipLevelEnum = VipUtils.getByLevel(createVipOrderRequest.getVipLevel());
        boolean canUpgrade = VipUtils.canUpgrade(currentVipLevelEnum, targetVipLevelEnum);
        if (!canUpgrade) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "VIP无法降级");
        }
        // 创建VIP订单： TODO 订单过期 自动过期功能
        return VipOrderUtils.createOrder(loginUser.getId(), targetVipLevelEnum, "用户创建订单购买会员");
    }

    /**
     * 支付功能
     * 1. 查询订单是否未支付 已过期 已取消
     * 2. 未支付状态可以支付
     * 3. 支付成功 更新订单状态
     * @param payVipOrderRequest
     * @param loginUser
     * @return
     */
    @Override
    public String payBuyVipOrder(PayVipOrderRequest payVipOrderRequest, User loginUser) {
        // 订单号
        String orderNo = payVipOrderRequest.getOrderNo();
        // 用户支付的金额
        int amount = payVipOrderRequest.getAmount();
        VipOrder order = this.lambdaQuery()
                .eq(VipOrder::getOrderNo, orderNo)
                .one();

        if(order.getPayStatus()==VipOrderUtils.PAY_STATUS_PAID){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请勿重复支付");
        }
        if(order.getPayStatus()==VipOrderUtils.PAY_STATUS_EXPIRED){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单已过期");
        }
        if(order.getPayStatus()==VipOrderUtils.PAY_STATUS_CANCELLED){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单已取消");
        }
        if(order.getAmount()!=amount){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "支付金额错误");
        }
        // 乐观锁最后校验 避免并发问题 如果更新失败 请用户重新刷新界面 查询订单状态
        boolean update = this.lambdaUpdate()
                .eq(VipOrder::getOrderNo, orderNo)
                .eq(VipOrder::getPayStatus, VipOrderUtils.PAY_STATUS_UNPAID)
                .set(VipOrder::getPayStatus, VipOrderUtils.PAY_STATUS_PAID)
                .update();
        if(!update){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单支付错误，请勿重复提交，请刷新界面查看订单支付状态");
        }
        VipOrderMessage message = new VipOrderMessage();
        message.setVipLevel(order.getVipLevel());
        message.setUserId(loginUser.getId());
        message.setOrderNo(orderNo);
        rabbitTemplate.convertAndSend("vip.order.topic","vip.order.buy", message);
        return order.getOrderNo();
    }

    @Override
    public VipOrder getVipOrderByOrderNo(String orderNo) {
        return this.lambdaQuery().eq(VipOrder::getOrderNo, orderNo).one();
    }

    @Override
    public void consumeVipOrder(String orderNo) {
        this.lambdaUpdate()
                .eq(VipOrder::getOrderNo, orderNo)
                .set(VipOrder::getPayStatus, VipOrderUtils.PAY_STATUS_CONSUMED)
                .update();
    }
}





