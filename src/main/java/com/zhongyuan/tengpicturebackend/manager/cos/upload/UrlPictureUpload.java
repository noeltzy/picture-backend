package com.zhongyuan.tengpicturebackend.manager.cos.upload;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.zhongyuan.tengpicturebackend.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@Service
public class UrlPictureUpload extends PictureUploadTemplate {

    private long fileSize;

    @Override
    protected long getFileSize(Object inputSource) {
        return this.fileSize;
    }

    @Override
    protected void save2tmpFile(Object inputSource, File tmpFile) throws IOException {
        String fileUrl = (String) inputSource;
        fileSize = HttpUtil.downloadFile(fileUrl, tmpFile);
    }

    @Override
    protected String getFullFileName(Object inputSource) {
        String fileUrl = (String) inputSource;
        return FileNameUtil.getName(fileUrl);
    }

    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        // 1. 校验非空
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.SYSTEM_ERROR, "图片不能为空");
        // 2. 校验URl格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片url格式错误");
        }
        // 3. 校验url协议
        ThrowUtils.throwIf(!fileUrl.startsWith("https://") && !fileUrl.startsWith("http://"),
                ErrorCode.PARAMS_ERROR, "图片url协议错误");

        // 4. 校验url是否可访问 发送Head请求
        try (HttpResponse response = HttpUtil.createRequest(Method.HEAD, fileUrl)
                .execute()) {
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            //5. 文件类型
            String contentType = response.header("Content-Type");
            ThrowUtils.throwIf(!contentType.startsWith("image/"), ErrorCode.PARAMS_ERROR, "图片类型错误");
            //6. 文件大小
            try {
                long contentLength = Long.parseLong(response.header("Content-Length"));
                ThrowUtils.throwIf(contentLength > 1024 * 1024 * 5, ErrorCode.PARAMS_ERROR, "图片大小不能超过5M");
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片大小格式失败");
            }
        }

    }
}
