package com.zhongyuan.tengpicturebackend.vip.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.vip.model.entity.VipOrder;
import com.zhongyuan.tengpicturebackend.vip.model.enums.VipLevelEnum;

import java.util.Date;

public class VipOrderUtils {

    /**
     * 支付状态枚举
     */
    public static final int PAY_STATUS_UNPAID = 0;
    public static final int PAY_STATUS_PAID = 1;
    public static final int PAY_STATUS_EXPIRED = 2;
    /**
     * 订单已经消费
     */
    public static final int PAY_STATUS_CONSUMED = 3;// 消费
    public static final int PAY_STATUS_CANCELLED = 4;//添加过期状态
    public static final int PAY_STATUS_REFUNDED = 5; // 订单退费

    public static VipOrder createOrder(Long userId, VipLevelEnum vipLevel) {
        // 参数校验
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户信息不能为空");
        }
        if (vipLevel == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "VIP等级不能为空");
        }

        VipOrder order = new VipOrder();
        // 设置基本信息
        order.setUserId(userId);
        order.setOrderNo(generateOrderNo());
        order.setVipLevel(vipLevel.getLevel());
        order.setAmount(vipLevel.getPrice());
        order.setPayStatus(PAY_STATUS_UNPAID);

        return order;
    }

    public static VipOrder createOrder(Long userId, VipLevelEnum vipLevel, String remark) {
        VipOrder order = createOrder(userId, vipLevel);
        order.setRemark(remark);
        return order;
    }

    private static String generateOrderNo() {
        // 使用Hutool的雪花算法生成器，workerId和dataCenterId从0-31之间选择
        Snowflake snowflake = IdUtil.getSnowflake(1, 1);
        String snowflakeId = String.valueOf(snowflake.nextId());

        // 使用Redis自增序列生成4位序列号，每天从1开始计数
        String dateKey = DateUtil.format(new Date(), "yyyyMMdd");
        String redisKey = "vip:order:seq:" + dateKey;
        String sequenceStr = RandomUtil.randomNumbers(4);
        // 组合订单号：VIP(3位) + 雪花ID(13位) + 序列号(4位) = 20位
        return "VIP" + snowflakeId.substring(snowflakeId.length() - 13) + sequenceStr;
    }
}