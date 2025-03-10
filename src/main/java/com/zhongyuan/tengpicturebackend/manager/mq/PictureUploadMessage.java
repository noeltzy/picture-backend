package com.zhongyuan.tengpicturebackend.manager.mq;

import com.zhongyuan.tengpicturebackend.model.dto.picture.PictureUploadRequest;
import com.zhongyuan.tengpicturebackend.model.entity.User;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
public class PictureUploadMessage implements Serializable {
    String messageId;
    PictureUploadRequest pictureUploadRequest;
    MultipartFile file;
    User loginUser;
}
