package com.zhongyuan.tengpicturebackend.common.message;


import lombok.Data;

import java.io.Serializable;

@Data
public class PictureAiMessage implements Serializable {
    private static final long serialVersionUID = -4804568217879896076L;

    Long TaskId;
    Long UserId;
    Long SpaceId;
    String prompt;
    int taskType;
    // 用于恢复
    Long UserVipId;
}
