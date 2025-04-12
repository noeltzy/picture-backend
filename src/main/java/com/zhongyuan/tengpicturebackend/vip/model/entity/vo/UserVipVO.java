package com.zhongyuan.tengpicturebackend.vip.model.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongyuan.tengpicturebackend.vip.model.entity.UserVip;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;

/**
 * 用户VIP表
 * @TableName user_vip
 */
@TableName(value ="user_vip")
@Data
public class UserVipVO {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * VIP等级：0-免费用户 1-VIP1 2-VIP2 3-VIP3
     */
    private Integer vipLevel;

    /**
     * Token总额度
     */
    private Long totalTokens;

    /**
     * 已使用Token数量
     */
    private Long usedTokens;

    /**
     * 每日可调用次数
     */
    private Integer dailyLimit;

    /**
     * 生效时间
     */
    private Date startTime;

    /**
     * 到期时间
     */
    private Date endTime;

    /**
     * 状态：0-已失效 1-生效中 2-已过期
     */
    private Integer status;


    public  static UserVipVO convert(UserVip userVip){
        UserVipVO userVipVO = new UserVipVO();
        BeanUtils.copyProperties(userVip,userVipVO);
        return userVipVO;
    }

}