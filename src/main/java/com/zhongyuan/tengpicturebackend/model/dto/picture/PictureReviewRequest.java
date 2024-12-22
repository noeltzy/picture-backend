package com.zhongyuan.tengpicturebackend.model.dto.picture;


import lombok.Data;

import java.io.Serializable;

@Data
public class PictureReviewRequest implements Serializable {
    private static final long serialVersionUID = -3202021162064733827L;

    /**
     * 图片 id
     */
    private Long id;
    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝S
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;
}
