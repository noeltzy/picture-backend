package com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadByBatchRequest implements Serializable {
    private static final long serialVersionUID = 7851202832278239537L;

    /**
     * 抓取 搜索关键词
     */
    private String searchText;

    /**
     * 抓取数量
     */
    private Integer count;

    /**
     * 导入图片名称
     */
    private String prefixName;
    /**
     * 导入分类
     */
    private String category;
    /**
     * 导入标签
     */
    private String[] tag;
}