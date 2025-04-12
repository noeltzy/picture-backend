package com.zhongyuan.tengpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongyuan.tengpicturebackend.manager.api.aliyunai.model.common.CreateTaskResponse;
import com.zhongyuan.tengpicturebackend.manager.api.aliyunai.model.genPicture.GenPictureRequest;
import com.zhongyuan.tengpicturebackend.manager.api.aliyunai.model.genPicture.ImageGenerationResponse;
import com.zhongyuan.tengpicturebackend.manager.api.aliyunai.model.outPainting.GetOutPaintingTaskResponse;
import com.zhongyuan.tengpicturebackend.model.dto.picture.*;
import com.zhongyuan.tengpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhongyuan.tengpicturebackend.model.entity.User;
import com.zhongyuan.tengpicturebackend.model.enums.SpaceRoleEnum;
import com.zhongyuan.tengpicturebackend.model.vo.PictureVo;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;

/**
* @author Windows11
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2024-12-20 14:12:33
*/
public interface PictureService extends IService<Picture> {


    PictureVo uploadPicture(Object inputSource, PictureUploadRequest uploadRequest, User loginUser);

    void validPicture(Picture picture);

    LambdaQueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     *  图片审核
     * @param pictureReviewRequest 请求参数
     * @param user 当前用户
     */
    void reviewPicture(PictureReviewRequest pictureReviewRequest,User user);

    void setReviewParam(Picture picture, User loginUser);

    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);
    void checkPictureOptionAuth(Picture picture, User loginUser, SpaceRoleEnum requestRole);

    void deletePicture(Long id, User loginUser);

    void tryClearPictureFile(Picture picture);

    CreateTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);
    CreateTaskResponse createGenPictureTask(GenPictureRequest request, User loginUser);

    GetOutPaintingTaskResponse getOutPaintingResult(String taskId);

    ImageGenerationResponse getGenerationResult(String taskId);

    PictureVo getPictureVoById(long id, HttpServletRequest request);

    Page<PictureVo> listPictureVoPage(PictureQueryRequest pictureQueryRequest, HttpServletRequest request);

    boolean editPicture(PictureEditRequest pictureEditRequest, HttpServletRequest request);

    String downloadPicture(Long id, HttpServletRequest request) throws MalformedURLException;

    PictureVo uploadPictureMq(Object file, PictureUploadRequest pictureUploadRequest, User loginUser);
}
