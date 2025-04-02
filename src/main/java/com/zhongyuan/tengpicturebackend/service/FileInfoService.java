package com.zhongyuan.tengpicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhongyuan.tengpicturebackend.model.dto.file.PictureUploadResult;
import com.zhongyuan.tengpicturebackend.model.entity.FileInfo;

import java.io.File;

/**
* @author Windows11
* @description 针对表【file_info】的数据库操作Service
* @createDate 2025-04-02 13:04:33
*/
public interface FileInfoService extends IService<FileInfo> {
     FileInfo checkUpload(File file);

     FileInfo checkUpload(String md5);

     int upload(String md5);
     int remove(String md5);


    void uploadNewFile(PictureUploadResult uploadResult, String md5Hex);
}
