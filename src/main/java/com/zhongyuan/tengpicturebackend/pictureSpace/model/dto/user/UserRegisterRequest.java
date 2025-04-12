package com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.user;


import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = -8008605264640853812L;


    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    private String checkPassword;
}
