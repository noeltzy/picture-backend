package com.zhongyuan.tengpicturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.zhongyuan.tengpicturebackend.config.CosConfig;
import com.zhongyuan.tengpicturebackend.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.exception.ThrowUtils;
import com.zhongyuan.tengpicturebackend.manager.cos.CosManager;
import com.zhongyuan.tengpicturebackend.model.dto.file.PictureUploadResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@Deprecated
public class FileManager {
    final long _1M = 1024 * 1024;
    final List<String> IMG_ALLOW_FILE_SUFFIX = Arrays.asList("jpg", "png", "jpeg", "gif", "webp", "svg");

    @Resource
    private CosConfig cosConfig;
    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     *
     * @param file             文件MultipartFile
     * @param uploadPathPrefix 上传路径前缀
     * @return 上传图像信息
     */
    public PictureUploadResult uploadPicture(MultipartFile file, String uploadPathPrefix) {
        // 校验图片
        validPicture(file);
        //拼接图片访问地址
        String uuid = RandomUtil.randomNumbers(12);
        String originalFilename = file.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, suffix);
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
        //上传图片
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile(uploadPath, null);
            // 将 MultipartFile 保存到临时的file
            file.transferTo(tmpFile);
            PutObjectResult putObjectResult = cosManager.putPictureObject(tmpFile, uploadPath);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

            String format = imageInfo.getFormat();
            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();
            PictureUploadResult pictureUploadResult = new PictureUploadResult();
            double picScale = NumberUtil.round(width * 1.0 / height, 2).doubleValue();
            pictureUploadResult.setUrl(cosConfig.getHost() + uploadPath);
            pictureUploadResult.setPicName(FileUtil.mainName(originalFilename));
            pictureUploadResult.setPicFormat(format);
            pictureUploadResult.setPicSize(file.getSize());
            pictureUploadResult.setPicWidth(width);
            pictureUploadResult.setPicHeight(height);
            pictureUploadResult.setPicScale(picScale);
            return pictureUploadResult;
        } catch (IOException e) {
            log.error("file upload error,path:{}", originalFilename, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        } finally {
            // 删除临时文件
            deleteTempFile(tmpFile);

        }

    }

    public void validPicture(MultipartFile file) {
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.SYSTEM_ERROR, "图片不能为空");
        // 尺寸校验:
        ThrowUtils.throwIf(file.getSize() > _1M * 2, ErrorCode.SYSTEM_ERROR, "图片大小不能超过2M");
        // 获取文件后缀
        String fileSuffix = FileUtil.getSuffix(file.getOriginalFilename());
        // 校验文件后缀
        ThrowUtils.throwIf(!IMG_ALLOW_FILE_SUFFIX.contains(fileSuffix), ErrorCode.SYSTEM_ERROR, "图片格式不正确");
    }


    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean delete = file.delete();
        ThrowUtils.throwIf(!delete, ErrorCode.SYSTEM_ERROR, "临时文件删除失败");
    }

}
