package com.zhongyuan.tengpicturebackend.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.zhongyuan.tengpicturebackend.config.CosConfig;
import com.zhongyuan.tengpicturebackend.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.exception.ThrowUtils;
import com.zhongyuan.tengpicturebackend.manager.CosManager;
import com.zhongyuan.tengpicturebackend.model.dto.file.PictureUploadResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public abstract class PictureUploadTemplate {
    final long _1M = 1024 * 1024;
    final List<String> IMG_ALLOW_FILE_SUFFIX = Arrays.asList("jpg", "png", "jpeg", "gif", "webp", "svg");

    @Resource
    private CosConfig cosConfig;
    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     *
     * @param inputSource 文件元
     * @param uploadPathPrefix 上传路径前缀
     * @return 上传图像信息
     */
    public PictureUploadResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1. TODO 校验图片
        validPicture(inputSource);
        //拼接图片访问地址
        String uuid = RandomUtil.randomNumbers(12);
        //TODO 获取originalFilename
        String originalFilename = getFullFileName(inputSource);

        String extName = FileNameUtil.extName(originalFilename);
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, extName);
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
        //上传图片
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile(uploadPath, null);
            //TODO  处理文件来源
            save2tmpFile(inputSource, tmpFile);
            // 上传到cos 并返回信息
            return getPictureUploadResult(tmpFile, uploadPath, originalFilename);
        } catch (IOException e) {
            log.error("file upload error,path:{}", originalFilename, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        } finally {
            // 删除临时文件
            deleteTempFile(tmpFile);

        }

    }

    /**
     * 上传本地图像文件到cos
     *
     * @param tmpFile          本地文件
     * @param uploadPath       上传路径
     * @param originalFilename 原始文件名
     * @return 上传结果
     */
    private PictureUploadResult getPictureUploadResult(File tmpFile, String uploadPath, String originalFilename) {
        // 1. 上传文件到cos
        PutObjectResult putObjectResult = cosManager.putPictureObject(tmpFile, uploadPath);
        // 2. 获取图片信息
        ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
        String format = imageInfo.getFormat();
        int width = imageInfo.getWidth();
        int height = imageInfo.getHeight();
        // 3 填入图片上传结果
        PictureUploadResult pictureUploadResult = new PictureUploadResult();
        double picScale = NumberUtil.round(width * 1.0 / height, 2).doubleValue();
        pictureUploadResult.setUrl(cosConfig.getHost() + uploadPath);
        pictureUploadResult.setPicName(FileUtil.mainName(originalFilename));
        pictureUploadResult.setPicFormat(format);
        pictureUploadResult.setPicSize(FileUtil.size(tmpFile));
        pictureUploadResult.setPicWidth(width);
        pictureUploadResult.setPicHeight(height);
        pictureUploadResult.setPicScale(picScale);
        return pictureUploadResult;
    }

    /**
     * 保存文件到临时目录
     *
     * @param inputSource 图片文件来源 String：url/MultipartFile：文件
     * @param tmpFile     临时文件
     */
    protected abstract void save2tmpFile(Object inputSource, File tmpFile) throws IOException;

    /**
     * 获取文件全名有后缀带后缀
     *
     * @param inputSource 图片文件来源 String：url/MultipartFile：文件
     * @return 全名
     */
    protected abstract String getFullFileName(Object inputSource);

    /**
     * 校验图片By url
     *
     * @param inputSource 图片文件来源 String：url/MultipartFile：文件
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 删除本地上传的临时文件
     *
     * @param file 文件对象
     */
    private void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean delete = file.delete();
        ThrowUtils.throwIf(!delete, ErrorCode.SYSTEM_ERROR, "临时文件删除失败");
    }

}
