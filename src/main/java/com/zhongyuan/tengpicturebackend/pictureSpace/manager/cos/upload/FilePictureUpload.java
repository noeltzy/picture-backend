package com.zhongyuan.tengpicturebackend.pictureSpace.manager.cos.upload;

import cn.hutool.core.io.FileUtil;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;


@Service
public class FilePictureUpload extends PictureUploadTemplate {
    @Override
    protected long getFileSize(Object inputSource) {
        MultipartFile file = (MultipartFile) inputSource;
        return file.getSize();
    }

    @Override
    protected void save2tmpFile(Object inputSource, File tmpFile) throws IOException {
        MultipartFile file = (MultipartFile) inputSource;
        file.transferTo(tmpFile);
    }

    @Override
    protected String getFullFileName(Object inputSource) {
        MultipartFile file = (MultipartFile) inputSource;
        return file.getOriginalFilename();
    }

    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile file = (MultipartFile) inputSource;
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.SYSTEM_ERROR, "图片不能为空");
        // 尺寸校验:
        ThrowUtils.throwIf(file.getSize() > _1M * 2, ErrorCode.SYSTEM_ERROR, "图片大小不能超过2M");
        // 获取文件后缀
        String fileSuffix = FileUtil.getSuffix(file.getOriginalFilename());
        // 校验文件后缀
        ThrowUtils.throwIf(!IMG_ALLOW_FILE_SUFFIX.contains(fileSuffix), ErrorCode.SYSTEM_ERROR, "图片格式不正确");

    }
}
