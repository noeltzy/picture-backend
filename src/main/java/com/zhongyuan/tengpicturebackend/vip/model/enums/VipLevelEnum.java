package com.zhongyuan.tengpicturebackend.vip.model.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * VIP等级枚举
 */
@Getter
@AllArgsConstructor
public enum VipLevelEnum {
    FREE(0, "免费用户", 5L, 5, 0, "基础图片生成功能", 1),
    VIP1(1, "基础会员", 200L, 20, 500, "高级图片生成功能", 2),
    VIP2(2, "专业会员", 1000L, 100, 2000, "专业图片生成功能", 3);
    private final Integer level;
    private final String name;
    private final Long monthlyTokens;
    private final Integer dailyLimit;
    private final Integer price;
    private final String benefits;
    private final Integer priority;
}