package com.zhongyuan.tengpicturebackend.model.dto.picture;

import com.zhongyuan.tengpicturebackend.api.aliyunai.model.outPainting.CreateOutPaintingTaskRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class CreatePictureOutPaintingTaskRequest implements Serializable {

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 扩图参数
     */
    private CreateOutPaintingTaskRequest.Parameters parameters;

    private static final long serialVersionUID = 1L;
}
