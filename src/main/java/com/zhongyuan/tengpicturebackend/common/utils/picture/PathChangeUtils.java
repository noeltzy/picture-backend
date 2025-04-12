package com.zhongyuan.tengpicturebackend.common.utils.picture;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

public class PathChangeUtils {
    public static String getThumbUrlFromOriginalPath(String originalPath) {
        return StrUtil.replace(originalPath, "(\\.[a-zA-Z0-9]+)$", "_thumb$1");
    }

    public static String getUrlFromOriginalPath(String originalPath) {
        return StrUtil.replace(originalPath, "(\\.[a-zA-Z0-9]+)$", ".webp");
    }
}
