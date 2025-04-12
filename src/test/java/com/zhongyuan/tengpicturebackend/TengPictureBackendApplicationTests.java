package com.zhongyuan.tengpicturebackend;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.ciModel.common.ImageProcessRequest;
import com.qcloud.cos.model.ciModel.persistence.CIUploadResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.zhongyuan.tengpicturebackend.common.config.CosConfig;
import com.zhongyuan.tengpicturebackend.pictureSpace.manager.cos.CosManager;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.Picture;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.PictureService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@SpringBootTest
class TengPictureBackendApplicationTests {
    @Resource
    CosManager cosManager;

    @Resource
    COSClient cosClient;

    @Resource
    CosConfig cosConfig;
    @Resource
    PictureService pictureService;

    @Test
    void contextLoads() {
        boolean b = cosManager.checkObject("space/1898618113358733314/2025-03-25_351438186237.jpg");
        System.out.println(b);
    }

    @Test
    void contextLoads1() {
        String bucketName = cosConfig.getBucket();
        String key = "space/1898618113358733314/2025-03-25_351438186237.jpg";
        ImageProcessRequest imageReq = new ImageProcessRequest(bucketName, key);

        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1);
        List<PicOperations.Rule> ruleList = new LinkedList<>();
        PicOperations.Rule rule1 = new PicOperations.Rule();
        rule1.setBucket(bucketName);
        rule1.setFileId("webp");
        rule1.setRule("imageMogr2/format/webp");
        ruleList.add(rule1);
        picOperations.setRules(ruleList);
        imageReq.setPicOperations(picOperations);
        CIUploadResult result = cosClient.processImage(imageReq);
        System.out.println(result);
        System.out.println(result.getOriginalInfo());
    }

    @Test
    void getPictureSize() {
        String bucketName = cosConfig.getBucket();
        String key = "space/1898618294095486978/2025-04-01_600580521379.png";
        ObjectMetadata metadata = cosClient.getObjectMetadata(bucketName, key);
        System.out.println("文件大小: " + metadata.getContentLength() + " 字节");
        LambdaQueryWrapper<Picture> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Picture::getOriginUrl,key);
        Picture one = pictureService.getOne(queryWrapper);
        System.out.println(one.getPicSize());
    }


}
