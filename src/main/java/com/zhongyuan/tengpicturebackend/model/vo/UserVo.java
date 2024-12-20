package com.zhongyuan.tengpicturebackend.model.vo;

import com.zhongyuan.tengpicturebackend.model.entity.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户
 */

@Data
public class UserVo implements Serializable {
    private static final long serialVersionUID = -4706257964158348900L;
    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    private String phone;


    /**
     * 转换
     * @param user 用户
     * @return vo
     */
    public static UserVo convert(User user) {
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        return userVo;
    }
}