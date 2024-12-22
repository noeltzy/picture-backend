package com.zhongyuan.tengpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongyuan.tengpicturebackend.model.dto.picture.PictureQueryRequest;
import com.zhongyuan.tengpicturebackend.model.dto.picture.PictureReviewRequest;
import com.zhongyuan.tengpicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.zhongyuan.tengpicturebackend.model.dto.picture.PictureUploadRequest;
import com.zhongyuan.tengpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhongyuan.tengpicturebackend.model.entity.User;
import com.zhongyuan.tengpicturebackend.model.vo.PictureVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Windows11
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2024-12-20 14:12:33
*/
public interface PictureService extends IService<Picture> {


    PictureVo uploadPicture(Object inputSource, PictureUploadRequest uploadRequest, User loginUser);

    void validPicture(Picture picture);

    LambdaQueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    List<PictureVo> toVoList(List<Picture> records, HttpServletRequest request);

    /**
     *  图片审核
     * @param pictureReviewRequest 请求参数
     * @param user 当前用户
     */
    void reviewPicture(PictureReviewRequest pictureReviewRequest,User user);

    void setReviewParam(Picture picture, User loginUser);

    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);
}
