package com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.ai;

import lombok.Data;

@Data
public class GenPictureTaskRequest {
    private String prompt;
    private Long SpaceId;
}
