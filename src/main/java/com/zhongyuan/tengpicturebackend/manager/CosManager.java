package com.zhongyuan.tengpicturebackend.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.zhongyuan.tengpicturebackend.config.CosConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

@Component
public class CosManager {
    @Resource
    private CosConfig cosConfig;

    @Resource
    private COSClient cosClient;

    // 将本地文件上传到 COS

    /**
     *  将本地文件上传到 COS
     * @param key 文件名/文件位置
     * @param file java文件对象
     * @return PutObjectResult
     */
    public PutObjectResult putObject( String key, File file){
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosConfig.getBucket(), key, file);
        return  cosClient.putObject(putObjectRequest);
    }

    public PutObjectResult putPictureObject( File file,String key){
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosConfig.getBucket(), key, file);
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1);
        putObjectRequest.setPicOperations(picOperations);
        return  cosClient.putObject(putObjectRequest);
    }
}
