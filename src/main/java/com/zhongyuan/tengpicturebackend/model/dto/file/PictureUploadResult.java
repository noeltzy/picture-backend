package com.zhongyuan.tengpicturebackend.model.dto.file;


import com.zhongyuan.tengpicturebackend.model.entity.Picture;
import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadResult implements Serializable {
    private static final long serialVersionUID = 2539519069025394140L;
    /**
     * 图片的url
     */
    private String url;
    /**
     * 图片的名称
     */
    private String PicName;
    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 图片大小
     */
    private long picSize;

    /**
     * 图片宽度
     */
    private int picWidth;
    /**
     * 图片高度
     */
    private int picHeight;

    /**
     * 缩略图 url
     */
    private String thumbnailUrl;

    /**
     * 图片宽高比例
     */
    private double picScale;

    public static Picture toPicture(PictureUploadResult pictureUploadResult, Long UserId){
        Picture picture = new Picture();
        picture.setUrl(pictureUploadResult.getUrl());
        picture.setName(pictureUploadResult.getPicName());
        picture.setPicSize(pictureUploadResult.getPicSize());
        picture.setPicWidth(pictureUploadResult.getPicWidth());
        picture.setPicHeight(pictureUploadResult.getPicHeight());
        picture.setPicScale(pictureUploadResult.getPicScale());
        picture.setPicFormat(pictureUploadResult.getPicFormat());
        picture.setThumbnailUrl(pictureUploadResult.getThumbnailUrl());
        picture.setUserId(UserId);
        return  picture;
    }

}
