package com.zhongyuan.tengpicturebackend.message;

import com.zhongyuan.tengpicturebackend.utils.picture.PictureProcessRuleEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureProcessMessage implements Serializable {
    private static final long serialVersionUID = 7173401751934890461L;
    private String originPictureKey;
    private long pictureId;
    private List<PictureProcessRuleEnum> processRules;
}
