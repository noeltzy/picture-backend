package com.zhongyuan.tengpicturebackend.pictureSpace.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongyuan.tengpicturebackend.pictureSpace.mapper.FileInfoMapper;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.file.PictureUploadResult;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.FileInfo;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.FileInfoService;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * @author Windows11
 * @description 针对表【file_info】的数据库操作Service实现
 * @createDate 2025-04-02 13:04:33
 */
@Service
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo>
        implements FileInfoService {

    @Override
    public FileInfo checkUpload(File file) {
        String s = DigestUtil.md5Hex(file);
        return this.checkUpload(s);
    }

    @Override
    public FileInfo checkUpload(String md5) {
        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getFileHash, md5);
        return this.getOne(queryWrapper);
    }

    @Override
    public int upload(String md5) {
        this.lambdaUpdate().eq(FileInfo::getFileHash,md5).setSql("uploadCount = uploadCount +1").update();
        return this.getBaseMapper().getUploadCountByHash(md5);
    }

    @Override
    public int remove(String md5) {
        int count = this.getBaseMapper().getUploadCountByHash(md5);
        if(count>1){
            this.lambdaUpdate().eq(FileInfo::getFileHash,md5).setSql("uploadCount = uploadCount -1").update();
            return  count -1;
        }
        else {
            LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(FileInfo::getFileHash, md5);
            return  0;
        }
    }

    @Override
    public void uploadNewFile(PictureUploadResult uploadResult, String md5Hex) {
        String picName = uploadResult.getPicName();
        String picFormat = uploadResult.getPicFormat();
        long picSize = uploadResult.getPicSize();
        int picWidth = uploadResult.getPicWidth();
        int picHeight = uploadResult.getPicHeight();
        String originUrl = uploadResult.getOriginUrl();
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName(picName);
        fileInfo.setFileSize(picSize);
        fileInfo.setFileHash(md5Hex);
        fileInfo.setFileUrl(originUrl);
        fileInfo.setFileHeight(picHeight);
        fileInfo.setFileWidth(picWidth);
        fileInfo.setFileFormat(picFormat);
        try {
            this.save(fileInfo);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}




