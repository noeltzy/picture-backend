package com.zhongyuan.tengpicturebackend.pictureSpace.manager.cos;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.zhongyuan.tengpicturebackend.common.config.CosConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;



@Component
@Slf4j
public class CosManager {
    @Resource
    private CosConfig cosConfig;

    @Resource
    private COSClient cosClient;

    /**
     *
     * @param key 删除为key值存储的对象
     */
    public void  deleteObject(String key){
        if(checkObject(key)){
            cosClient.deleteObject(cosConfig.getBucket(),key);
        }
    }

    // 检测key的纯在
    public boolean checkObject(String key){
        return cosClient.doesObjectExist(cosConfig.getBucket(),key);
    }


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
        // 返回原图信息
        picOperations.setIsPicInfo(1);
        // 构造压缩规则
        List<PicOperations.Rule> rules = new ArrayList<>();
        String webpKey = FileUtil.mainName(key) + ".webp";
        // 压缩格式为 webp
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setRule("imageMogr2/format/webp");
        compressRule.setBucket(cosConfig.getBucket());
        compressRule.setFileId(webpKey);
        rules.add(compressRule);
        // 添加缩略图功能
        if(file.length()>20*1024){
            log.info("处理缩略图,文件大小：{} KB",file.length()/1024);
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            thumbnailRule.setBucket(cosConfig.getBucket());
            String thumbnailKey = FileUtil.mainName(key) + "_thumb."+FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>",128,128));
            rules.add(thumbnailRule);
        }
        // 构造参数获取
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return  cosClient.putObject(putObjectRequest);
    }

    public PutObjectResult putPictureObjectDelayProcess( File file,String key){
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosConfig.getBucket(), key, file);
        PicOperations picOperations = new PicOperations();
        // 返回原图信息
        picOperations.setIsPicInfo(1);
        putObjectRequest.setPicOperations(picOperations);
        return  cosClient.putObject(putObjectRequest);
    }
}
