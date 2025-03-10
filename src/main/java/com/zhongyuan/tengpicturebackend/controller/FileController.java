package com.zhongyuan.tengpicturebackend.controller;


import com.zhongyuan.tengpicturebackend.annotation.AuthCheck;
import com.zhongyuan.tengpicturebackend.common.BaseResponse;
import com.zhongyuan.tengpicturebackend.common.ResultUtils;
import com.zhongyuan.tengpicturebackend.constant.UserConstant;
import com.zhongyuan.tengpicturebackend.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.manager.cos.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/file")
@Slf4j
@Deprecated
public class FileController {
    @Resource
    CosManager cosManager;

    @PostMapping("/test/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> testUploadFile(@RequestParam("file") MultipartFile file) {
        // 文件目录
        String fileName = file.getOriginalFilename();
        String filePath = String.format("/%s/%s", "test", fileName);
        // 创建临时文件对象
        File tmpFile = null;
        try {
            tmpFile=File.createTempFile(filePath, null);
            // 将 MultipartFile 保存到临时的file
            file.transferTo(tmpFile);
            cosManager.putObject(filePath,tmpFile);
        } catch (IOException e) {
            log.error("file upload error,path:{}", filePath,e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }finally {
            // 删除临时文件
            if (tmpFile != null) {
                boolean delete = tmpFile.delete();
                if (!delete) {
                    log.error("file delete error,path:{}", filePath);
                }
            }
        }
        return ResultUtils.success(filePath);
    }

}
