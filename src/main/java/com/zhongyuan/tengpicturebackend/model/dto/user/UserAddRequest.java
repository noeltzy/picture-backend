package com.zhongyuan.tengpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserAddRequest implements Serializable {

    private static final long serialVersionUID = -2309144946340437863L;
    /**
     * 账号
     */
    private String userAccount;

    /**
     * 手机号
     */
    private String phone;


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

}
