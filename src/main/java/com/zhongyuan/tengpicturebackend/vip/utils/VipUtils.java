package com.zhongyuan.tengpicturebackend.vip.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.unit.DataUnit;
import com.zhongyuan.tengpicturebackend.vip.model.entity.UserVip;
import com.zhongyuan.tengpicturebackend.vip.model.enums.VipLevelEnum;

import java.util.Date;

/**
 * VIP相关工具类
 */
public class VipUtils {

    /**
     * 根据VIP等级创建UserVip对象
     * 没设置 orderNo
     * @param userId 用户ID
     * @param level VIP等级
     * @return UserVip对象
     */
    public static UserVip createUserVip(Long userId, VipLevelEnum level) {
        UserVip userVip = new UserVip();
        // 设置基本信息
        userVip.setUserId(userId);
        userVip.setVipLevel(level.getLevel());
        userVip.setTotalTokens(level.getMonthlyTokens());
        userVip.setUsedTokens(0L);
        userVip.setDailyLimit(level.getDailyLimit());
        
        // 设置时间
        Date now = DateUtil.date();
        userVip.setStartTime(now);
        userVip.setEndTime(DateUtil.offsetMonth(now, 1)); // 默认一个月有效期
        userVip.setCreateTime(now);
        userVip.setUpdateTime(now);
        
        // 设置状态
        userVip.setStatus(1); // 1-生效中
        userVip.setIsDelete(0); // 0-未删除
        
        return userVip;
    }


    public static UserVip createUserVip(Long userId, VipLevelEnum level,UserVip oldVip,String orderNo) {
        UserVip userVip = createUserVip(userId, level);
        userVip.setTotalTokens(level.getMonthlyTokens() + oldVip.getTotalTokens());
        userVip.setUsedTokens(oldVip.getUsedTokens());
        userVip.setOrderNo(orderNo);
        return userVip;
    }

    /**
     * 根据VIP等级创建UserVip对象（指定时间段）
     *
     * @param userId 用户ID
     * @param level VIP等级
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return UserVip对象
     */
    public static UserVip createUserVip(Long userId, VipLevelEnum level, Date startTime, Date endTime) {
        UserVip userVip = createUserVip(userId, level);
        userVip.setStartTime(startTime);
        userVip.setEndTime(endTime);
        return userVip;
    }

    /**
     * 检查是否可以升级到目标等级
     *
     * @param currentLevel 当前等级
     * @param targetLevel 目标等级
     * @return 是否可以升级
     */
    public static boolean canUpgrade(VipLevelEnum currentLevel, VipLevelEnum targetLevel) {
        return canUpgrade(currentLevel.getLevel(), targetLevel.getLevel());
    }

    public static boolean canUpgrade(int currentLevel, int targetLevel) {
        return targetLevel > currentLevel;
    }


    /**
     * 获取价格（元，保留两位小数）
     *
     * @param level VIP等级
     * @return 价格字符串
     */
    public static String getPriceYuan(VipLevelEnum level) {
        return String.format("%.2f", level.getPrice() / 100.0);
    }

    /**
     * 检查token是否足够
     *
     * @param level VIP等级
     * @param currentUsed 当前已使用
     * @param required 需要使用
     * @return 是否足够
     */
    public static boolean isTokenEnough(VipLevelEnum level, Long currentUsed, int required) {
        return (level.getMonthlyTokens() - currentUsed) >= required;
    }

    /**
     * 检查日调用次数是否超限
     *
     * @param level VIP等级
     * @param currentCount 当前调用次数
     * @return 是否超限
     */
    public static boolean isDailyLimitExceeded(VipLevelEnum level, Integer currentCount) {
        return currentCount >= level.getDailyLimit();
    }

    /**
     * 根据等级获取VIP配置
     *
     * @param level 等级
     * @return VIP等级枚举
     */
    public static VipLevelEnum getByLevel(Integer level) {
        if (level == null) {
            return VipLevelEnum.FREE;
        }
        for (VipLevelEnum vipLevel : VipLevelEnum.values()) {
            if (vipLevel.getLevel().equals(level)) {
                return vipLevel;
            }
        }
        return VipLevelEnum.FREE;
    }
}