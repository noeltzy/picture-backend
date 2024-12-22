package com.zhongyuan.tengpicturebackend.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongyuan.tengpicturebackend.annotation.AuthCheck;
import com.zhongyuan.tengpicturebackend.common.BaseResponse;
import com.zhongyuan.tengpicturebackend.common.IdRequest;
import com.zhongyuan.tengpicturebackend.common.ResultUtils;
import com.zhongyuan.tengpicturebackend.constant.UserConstant;
import com.zhongyuan.tengpicturebackend.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.exception.ThrowUtils;
import com.zhongyuan.tengpicturebackend.model.dto.picture.*;
import com.zhongyuan.tengpicturebackend.model.entity.Picture;
import com.zhongyuan.tengpicturebackend.model.entity.User;
import com.zhongyuan.tengpicturebackend.model.enums.PictureReviewStatusEnum;
import com.zhongyuan.tengpicturebackend.model.vo.PictureTagCategory;
import com.zhongyuan.tengpicturebackend.model.vo.PictureVo;
import com.zhongyuan.tengpicturebackend.model.vo.UserVo;
import com.zhongyuan.tengpicturebackend.service.PictureService;
import com.zhongyuan.tengpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {
    @Resource
    PictureService pictureService;

    @Resource
    UserService userService;


    /**
     * 更新图片 管理员
     *
     * @param pictureUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureUpdateRequest.toObj();
        pictureService.validPicture(picture);
        Long picId = picture.getId();
        // 必须存在
        Picture oldPic = pictureService.getById(picId);
        ThrowUtils.throwIf(oldPic == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        User loginUser = userService.getLoginUser(request);
        pictureService.setReviewParam(picture, loginUser);
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    /**
     * 根据id获取对象 (admin)
     *
     * @param id 对象的id
     * @return 对象
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(picture);
    }


    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPicturePage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = pictureQueryRequest.getCurrent();
        int size = pictureQueryRequest.getPageSize();
        LambdaQueryWrapper<Picture> queryWrapper = pictureService.getQueryWrapper(pictureQueryRequest);
        Page<Picture> page = pictureService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(page);
    }

    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> reviewPicture(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.reviewPicture(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 批量上传图片(管理员)
     * @param pictureUploadByBatchRequest 请求参数
     * @param request 请求
     * @return 插入条数
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(
                                                 @RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest==null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        User loginUser = userService.getLoginUser(request);
        Integer count = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(count);
    }
    /**
     * 上传图片 or 更改图片
     *
     * @param file                 文件流
     * @param pictureUploadRequest 上传文件id
     * @param request              请求
     * @return 返回值
     */
    @PostMapping("/upload")
    public BaseResponse<PictureVo> uploadPicture(@RequestParam("file") MultipartFile file,
                                                 PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        User loginUser = userService.getLoginUser(request);
        PictureVo pictureVo = pictureService.uploadPicture(file, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVo);
    }

    /**
     * 上传图片 url
     * @param pictureUploadRequest url
     * @param request 请求体
     * @return 结果
     */
    @PostMapping("/upload/url")
    public BaseResponse<PictureVo> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadRequest == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        User loginUser = userService.getLoginUser(request);
        String url = pictureUploadRequest.getUrl();
        PictureVo pictureVo = pictureService.uploadPicture(url, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVo);
    }





    // 删除图片
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(idRequest == null || idRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 必须存在
        Picture oldPic = pictureService.getById(idRequest.getId());
        ThrowUtils.throwIf(oldPic == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        // 本人或者管理员
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!oldPic.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
        // 数据库删除
        boolean result = pictureService.removeById(idRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.NOT_FOUND_ERROR, "删除图片失败");
        return ResultUtils.success(true);
    }

    /**
     * 获取Vo
     *
     * @param id      图片id
     * @param request 请求
     * @return 图片vo
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVo> getPictureVoById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 必须存在且审核通过
        Picture picture = pictureService.lambdaQuery().eq(Picture::getId, id).one();
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        UserVo userVo = UserVo.obj2Vo(userService.getLoginUser(request));
        return ResultUtils.success(PictureVo.obj2Vo(picture, userVo));
    }

    /**
     * 分页查询图片VO
     *
     * @param pictureQueryRequest 查询请求
     * @param request             请求
     * @return 图片vo分页
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVo>> listPictureVoPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = pictureQueryRequest.getCurrent();
        int size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(current < 0 || size > 20, ErrorCode.PARAMS_ERROR);
        //设置查询条件一定是审核完毕的
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        LambdaQueryWrapper<Picture> queryWrapper = pictureService.getQueryWrapper(pictureQueryRequest);
        Page<Picture> page = pictureService.page(new Page<>(current, size), queryWrapper);
        Page<PictureVo> pictureVoPage = new Page<>(current, size, page.getTotal());
        pictureVoPage.setRecords(pictureService.toVoList(page.getRecords(), request));
        return ResultUtils.success(pictureVoPage);
    }


    /**
     * 编辑图片
     *
     * @param pictureEditRequest 编辑图片参数
     * @param request            请求
     * @return 结果
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editePicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditRequest == null, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureEditRequest.toObj();
        picture.setEditTime(new Date());
        pictureService.validPicture(picture);
        Long picId = picture.getId();
        // 必须存在
        Picture oldPic = pictureService.getById(picId);
        ThrowUtils.throwIf(oldPic == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        // 本人或者管理员
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!oldPic.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
        // 数据库操作
        pictureService.setReviewParam(picture, loginUser);
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> getTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tags = Arrays.asList("热门", "搞笑", "生活", "高清", "默认", "艺术");
        List<String> categories = Arrays.asList("模板", "电商", "动漫", "素材", "海报");
        pictureTagCategory.setTags(tags);
        pictureTagCategory.setCategories(categories);
        return ResultUtils.success(pictureTagCategory);
    }

}
