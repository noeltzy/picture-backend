package com.zhongyuan.tengpicturebackend.pictureSpace.utils.picture;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class PictureProcessUtils {

    public static List<PicOperations.Rule> getRuleList(List<PictureProcessRuleEnum> rules, String key, String bucket) {
        List<PicOperations.Rule> ruleList = new LinkedList<>();
        for (PictureProcessRuleEnum rule : rules) {
            switch (rule) {
                case COMPRESS:
                    PicOperations.Rule compressRule = new PicOperations.Rule();
                    String webpKey = FileUtil.mainName(key) + ".webp";
                    compressRule.setRule("imageMogr2/format/webp");
                    compressRule.setBucket(bucket);
                    compressRule.setFileId(webpKey);
                    ruleList.add(compressRule);
                    break;
                case THUMBNAIL:
                    PicOperations.Rule thumbnailRule = new PicOperations.Rule();
                    thumbnailRule.setBucket(bucket);
                    String thumbnailKey = FileUtil.mainName(key) + "_thumb." + FileUtil.getSuffix(key);
                    thumbnailRule.setFileId(thumbnailKey);
                    thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 128, 128));
                    ruleList.add(thumbnailRule);
                    break;
                default: {
                    log.info("无操作");
                }
            }
        }
        return ruleList;
    }
}
