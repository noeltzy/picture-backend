package com.zhongyuan.tengpicturebackend.pictureSpace.manager.cos.process;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ciModel.common.ImageProcessRequest;
import com.qcloud.cos.model.ciModel.persistence.CIUploadResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.zhongyuan.tengpicturebackend.common.config.CosConfig;
import com.zhongyuan.tengpicturebackend.common.message.PictureProcessMessage;
import com.zhongyuan.tengpicturebackend.common.utils.picture.PictureProcessRuleEnum;
import com.zhongyuan.tengpicturebackend.common.utils.picture.PictureProcessUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class PictureCompress {
    @Resource
    private CosConfig cosConfig;

    @Resource
    private COSClient cosClient;

    public CIUploadResult processPicture(PictureProcessMessage pictureProcessMessage) {
        String originPictureKey = pictureProcessMessage.getOriginPictureKey();
        List<PictureProcessRuleEnum> processRules = pictureProcessMessage.getProcessRules();
        PicOperations picOperations = new PicOperations();
        ImageProcessRequest imageReq = new ImageProcessRequest(cosConfig.getBucket(), originPictureKey);
        // 构造压缩规则
        List<PicOperations.Rule> rules = PictureProcessUtils.getRuleList(processRules,originPictureKey,cosConfig.getBucket());
        // 构造参数获取
        picOperations.setRules(rules);
        imageReq.setPicOperations(picOperations);
        return cosClient.processImage(imageReq);
    }
}
