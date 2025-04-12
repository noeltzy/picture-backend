package com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.picture;


import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {
    private static final long serialVersionUID = 3172055395056732987L;
    /**
     * 图片id
     * 用于更新图片
     * 为空则新增图片
     * 不为空则更新图片
     */
    private Long id;
    //url
    private String url;
    private Long spaceId;
    // 只提供给批量抓取图片使用
    private String batchFetchDefaultName;
    // 只提供给批量抓取图片使用
    private String category;

}
