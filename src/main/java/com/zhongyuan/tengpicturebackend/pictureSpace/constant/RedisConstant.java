package com.zhongyuan.tengpicturebackend.pictureSpace.constant;

/**
 * 用户常量
 */
public interface RedisConstant {
    String LIST_PICTURE_KEY_PREFIX = "TPB:listPictureVoPageWithCatch";
    // 单位 min
    Long LIST_PICTURE_TTL = 300L;

}
