package com.zhongyuan.tengpicturebackend.pictureSpace.model.entity;

import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.file.PictureUploadResult;
import com.zhongyuan.tengpicturebackend.pictureSpace.utils.picture.PathChangeUtils;
import lombok.Data;

/**
 * 
 * @TableName file_info
 */
@TableName(value ="file_info")
@Data
public class FileInfo {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String fileName;

    /**
     * 
     */
    private Long fileSize;

    /**
     * 
     */
    private String fileHash;

    /**
     * 
     */
    private String fileUrl;

    /**
     * 
     */
    private Integer uploadCount;

    /**
     * 
     */
    private Date uploadTime;

    /**
     * 
     */
    private Integer fileHeight;

    /**
     * 
     */
    private Integer fileWidth;

    /**
     * 
     */
    private String fileFormat;


    public static PictureUploadResult toPictureUploadResult(FileInfo  fileInfo) {
        PictureUploadResult result = new PictureUploadResult();
        double picScale = NumberUtil.round(fileInfo.getFileWidth() * 1.0 / fileInfo.getFileHeight(), 2).doubleValue();
        result.setPicName(fileInfo.getFileName());
        result.setPicFormat(fileInfo.getFileFormat());
        result.setPicSize(fileInfo.getFileSize());
        result.setPicWidth(fileInfo.getFileWidth());
        result.setPicHeight(fileInfo.getFileHeight());
        result.setOriginUrl(fileInfo.getFileUrl());
        result.setPicScale(picScale);
        result.setUrl(PathChangeUtils.getUrlFromOriginalPath(fileInfo.getFileUrl()));
        result.setThumbnailUrl(PathChangeUtils.getThumbUrlFromOriginalPath(fileInfo.getFileUrl()));
        return result;
    }
}