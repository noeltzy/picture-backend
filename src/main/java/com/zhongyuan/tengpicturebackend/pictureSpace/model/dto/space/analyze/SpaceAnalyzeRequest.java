package com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.space.analyze;


import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceAnalyzeRequest implements Serializable {
    private static final long serialVersionUID = 223548766263611152L;
    /**
     * 空间id
     * 若 queryAll 为true 或者 queryPublic 为true，则该字段无效
     */
    private Long spaceId;

    /**
     * 是否查询所有空间
     */
    private boolean queryAll;

    /**
     * 是否查询公开空间
     */
    private boolean queryPublic;
}
