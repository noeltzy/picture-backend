package com.zhongyuan.tengpicturebackend.model.dto.ai;

import lombok.Data;

@Data
public class GenPictureTaskRequest {
    private String prompt;
    private Long SpaceId;
}
