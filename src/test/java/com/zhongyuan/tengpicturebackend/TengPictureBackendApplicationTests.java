package com.zhongyuan.tengpicturebackend;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ciModel.common.ImageProcessRequest;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.CIUploadResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.zhongyuan.tengpicturebackend.api.aliyunai.service.AliYunApiService;
import com.zhongyuan.tengpicturebackend.config.CosConfig;
import com.zhongyuan.tengpicturebackend.manager.cos.CosManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

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
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;

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


}
