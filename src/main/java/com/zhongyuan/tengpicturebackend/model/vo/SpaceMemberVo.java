package com.zhongyuan.tengpicturebackend.model.vo;

import lombok.Data;

@Data
public class SpaceMemberVo {
    private Long userId;
    private String avatarUrl;  // 用户头像 URL
    private String spaceRole;  // 空间角色（viewer/editor/admin）
}
