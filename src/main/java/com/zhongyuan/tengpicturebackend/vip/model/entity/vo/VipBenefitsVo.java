package com.zhongyuan.tengpicturebackend.vip.model.entity.vo;

import com.zhongyuan.tengpicturebackend.vip.model.enums.VipLevelEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * VIP权益信息VO
 */
@Data
public class VipBenefitsVo implements Serializable {
    private static final long serialVersionUID = -1L;

    /**
     * VIP等级
     */
    private Integer level;

    /**
     * VIP名称
     */
    private String name;

    /**
     * 每月令牌数量
     */
    private Long monthlyTokens;

    /**
     * 每日使用限制
     */
    private Integer dailyLimit;

    /**
     * 价格（单位：分）
     */
    private Integer price;

    /**
     * 权益说明
     */
    private String benefits;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 从枚举转换为VO
     */
    public static VipBenefitsVo fromEnum(VipLevelEnum vipLevel) {
        VipBenefitsVo vo = new VipBenefitsVo();
        vo.setLevel(vipLevel.getLevel());
        vo.setName(vipLevel.getName());
        vo.setMonthlyTokens(vipLevel.getMonthlyTokens());
        vo.setDailyLimit(vipLevel.getDailyLimit());
        vo.setPrice(vipLevel.getPrice());
        vo.setBenefits(vipLevel.getBenefits());
        vo.setPriority(vipLevel.getPriority());
        return vo;
    }

    /**
     * 获取所有VIP权益列表
     */
    public static List<VipBenefitsVo> getAllBenefits() {
        return Stream.of(VipLevelEnum.values())
                .map(VipBenefitsVo::fromEnum)
                .collect(Collectors.toList());
    }
}