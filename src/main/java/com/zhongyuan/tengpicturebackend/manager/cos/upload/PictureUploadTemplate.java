package com.zhongyuan.tengpicturebackend.manager.cos.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import com.zhongyuan.tengpicturebackend.config.CosConfig;
import com.zhongyuan.tengpicturebackend.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.exception.ThrowUtils;
import com.zhongyuan.tengpicturebackend.manager.cos.CosManager;
import com.zhongyuan.tengpicturebackend.model.dto.file.PictureUploadResult;
import com.zhongyuan.tengpicturebackend.model.entity.FileInfo;
import com.zhongyuan.tengpicturebackend.service.FileInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    @Resource
    FileInfoService fileInfoService;

    /**
     * 上传图片
     *
     * @param inputSource      文件元
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
        String[] split = extName.split("\\?");
        extName = split[0];
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, extName);
        String originFileUploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
        //上传图片
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile(originFileUploadPath, null);
            save2tmpFile(inputSource, tmpFile);
            // TODO 查询Md5 如果存在实现秒传
            String md5Hex = DigestUtil.md5Hex(tmpFile);
            // 1. 首先判断是否是公共空间
            if(StrUtil.contains(originFileUploadPath,"public")){
                // 2. 判断是否是已经存在上传的记录了
                FileInfo fileInfo = fileInfoService.checkUpload(md5Hex);
                if(fileInfo != null){
                    log.info("秒传{}",fileInfo.getFileHash());
                    // 3. 如果有就返回文件信息 三个filePath,构建文件上传返回类,不用走文件上传流程
                    // 用于统计图片的上传次数
                    fileInfoService.upload(md5Hex);
                    return FileInfo.toPictureUploadResult(fileInfo);
                }
            }
            // 1. 上传文件到cos
            long currentTime = System.currentTimeMillis();
            PutObjectResult putObjectResult = cosManager.putPictureObject(tmpFile, originFileUploadPath);
            long contentLength = putObjectResult.getMetadata().getContentLength();
            putObjectResult.getMetadata().getContentMD5();
            long endTime = System.currentTimeMillis();
            log.info("图片{},上传完毕，共耗费时间：{}ms,图片大小为{}", originalFilename, endTime - currentTime, contentLength); //图片头像.jpg,上传完毕，共耗费时间：664ms
            //上传完毕
            PictureUploadResult uploadResult = getUploadResult(putObjectResult, originFileUploadPath);
            uploadResult.setPicName(FileUtil.mainName(originalFilename));
            long fileSize = getFileSize(inputSource);
            uploadResult.setPicSize(fileSize);
            // 从uploadResult 保存图片md5记录
            fileInfoService.uploadNewFile(uploadResult,md5Hex);

            return uploadResult;
        } catch (IOException e) {
            log.error("file upload error,path:{}", originalFilename, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        } finally {
            // 删除临时文件
            deleteTempFile(tmpFile);
        }

    }

    private PictureUploadResult getUploadResult(PutObjectResult putObjectResult, String originFileUploadPath) {

        PictureUploadResult pictureUploadResult = new PictureUploadResult();
        ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
        String format = imageInfo.getFormat();
        int width = imageInfo.getWidth();
        int height = imageInfo.getHeight();

        double picScale = NumberUtil.round(width * 1.0 / height, 2).doubleValue();
        pictureUploadResult.setPicWidth(width);
        pictureUploadResult.setPicHeight(height);
        pictureUploadResult.setPicScale(picScale);
        pictureUploadResult.setPicFormat(format);
        pictureUploadResult.setOriginUrl(cosConfig.getHost() + originFileUploadPath);
        ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
        List<CIObject> objectList = processResults.getObjectList();
        if (CollUtil.isNotEmpty(objectList)) {
            pictureUploadResult.setUrl(cosConfig.getHost() + "/" + objectList.get(0).getKey());
            if (objectList.size() > 1) {
                pictureUploadResult.setThumbnailUrl(cosConfig.getHost() + "/" + objectList.get(1).getKey());
            }
        }
        return pictureUploadResult;
    }


    public PictureUploadResult uploadPictureMq(Object inputSource, String uploadPathPrefix) {
        // 1. TODO 校验图片
        validPicture(inputSource);
        //拼接图片访问地址
        String uuid = RandomUtil.randomNumbers(12);
        String originalFilename = getFullFileName(inputSource);
        String extName = FileNameUtil.extName(originalFilename);
        String[] split = extName.split("\\?");
        extName = split[0];
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, extName);
        String originFileUploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
        //上传图片
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile(originFileUploadPath, null);
            save2tmpFile(inputSource, tmpFile);
            // 1. 上传文件到cos
            long currentTime = System.currentTimeMillis();
            PutObjectResult putObjectResult = cosManager.putPictureObjectDelayProcess(tmpFile, originFileUploadPath);
            long endTime = System.currentTimeMillis();
            log.info("图片{},上传完毕，共耗费时间：{}ms", originalFilename, endTime - currentTime); //图片头像.jpg,上传完毕，共耗费时间：664ms
            //上传完毕
            long fileSize = getFileSize(inputSource);
            PictureUploadResult uploadResult = getUploadResult(putObjectResult, originFileUploadPath);
            uploadResult.setPicName(FileUtil.mainName(originalFilename));
            uploadResult.setPicSize(fileSize);
            return uploadResult;
        } catch (IOException e) {
            log.error("file upload error,path:{}", originalFilename, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        } finally {
            // 删除临时文件
            deleteTempFile(tmpFile);
        }

    }

    protected abstract long getFileSize(Object inputSource);

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
