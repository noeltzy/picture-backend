package com.zhongyuan.tengpicturebackend.pictureSpace.controller;


import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongyuan.tengpicturebackend.common.annotation.AuthCheck;
import com.zhongyuan.tengpicturebackend.common.annotation.RequestLimit;
import com.zhongyuan.tengpicturebackend.common.model.BaseResponse;
import com.zhongyuan.tengpicturebackend.common.model.IdRequest;
import com.zhongyuan.tengpicturebackend.common.utils.ResultUtils;
import com.zhongyuan.tengpicturebackend.pictureSpace.constant.RedisConstant;
import com.zhongyuan.tengpicturebackend.pictureSpace.constant.UserConstant;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.ThrowUtils;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.picture.*;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.Picture;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.User;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.enums.PictureReviewStatusEnum;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.vo.PictureTagCategory;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.vo.PictureVo;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.PictureService;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {
    @Resource
    PictureService pictureService;

    @Resource
    UserService userService;

    @Resource
    StringRedisTemplate stringRedisTemplate;


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
     *
     * @param pictureUploadByBatchRequest 请求参数
     * @param request                     请求
     * @return 插入条数
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(
            @RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
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
     * 上传图片接口，限制每秒每用户5次
     */
    @RequestLimit(key = "pictureUpload", times = 5)
    @PostMapping("/upload")
    public BaseResponse<PictureVo> uploadPicture(@RequestParam("file") MultipartFile file,
                                                 PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        User loginUser = userService.getLoginUser(request);
        PictureVo pictureVo = pictureService.uploadPicture(file, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVo);
    }

    @RequestLimit(key = "pictureUpload", times = 5)
    @PostMapping("/upload/mq")
    public BaseResponse<PictureVo> uploadPictureMq(@RequestParam("file") MultipartFile file,
                                                   PictureUploadRequest pictureUploadRequest,
                                                   HttpServletRequest request) {
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        User loginUser = userService.getLoginUser(request);
        PictureVo pictureVo = pictureService.uploadPictureMq(file, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVo);
    }

    /**
     * 上传图片 url
     *
     * @param pictureUploadRequest url
     * @param request              请求体
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
        User loginUser = userService.getLoginUser(request);
        // 必须存在
        pictureService.deletePicture(idRequest.getId(), loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 获取Vo
     * 权限 check
     *
     * @param id      图片id
     * @param request 请求
     * @return 图片vo
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVo> getPictureVoById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        PictureVo pictureVo = pictureService.getPictureVoById(id, request);
        return ResultUtils.success(pictureVo);
    }

    /**
     * 分页查询图片VO
     * 权限 check
     *
     * @param pictureQueryRequest 查询请求
     * @param request             请求
     * @return 图片vo分页
     * TODO 公共空间图库可以设置走缓存，私有or团队的并发并不高，不用走缓存
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVo>> listPictureVoPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = pictureQueryRequest.getCurrent();
        int size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(current < 0 || size > 20, ErrorCode.PARAMS_ERROR);

        //service调用返回结果
        Page<PictureVo> result = pictureService.listPictureVoPage(pictureQueryRequest, request);

        return ResultUtils.success(result);
    }


    /**
     * 分页查询图片VO 有缓存
     * TODO 暂时留着 后续fix
     *
     * @param pictureQueryRequest 查询请求
     * @param request             请求
     * @return 图片vo分页
     */
    @PostMapping("/list/page/vo/catch")
    public BaseResponse<Page<PictureVo>> listPictureVoPageWithCatch(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = pictureQueryRequest.getCurrent();
        int size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(current < 0 || size > 20, ErrorCode.PARAMS_ERROR);
        //设置查询条件一定是审核完毕的
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        //查询缓存
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String redisKey = String.format("%s:%s", RedisConstant.LIST_PICTURE_KEY_PREFIX, hashKey);
        ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
        String cachedValue = stringStringValueOperations.get(redisKey);
        if (cachedValue != null) {
            // 缓存命中
            Page<PictureVo> pictureVoPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(pictureVoPage);
        }
        //查询数据库
        LambdaQueryWrapper<Picture> queryWrapper = pictureService.getQueryWrapper(pictureQueryRequest);
        Page<Picture> page = pictureService.page(new Page<>(current, size), queryWrapper);
        Page<PictureVo> pictureVoPage = new Page<>(current, size, page.getTotal());
        pictureVoPage.setRecords(PictureVo.toVoList(page.getRecords()));
        //缓存
        String catchValue = JSONUtil.toJsonStr(pictureVoPage);
        //! 设置随机缓存过期时间防止缓存雪崩 随机5-10分钟
        long expireTime = RedisConstant.LIST_PICTURE_TTL + RandomUtil.randomInt(0, 300);
        stringStringValueOperations.set(redisKey, catchValue, expireTime, TimeUnit.SECONDS);

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

        boolean result = pictureService.editPicture(pictureEditRequest, request);

        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        return ResultUtils.success(true);
    }


    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> getTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tags = Arrays.asList("热门", "搞笑", "生活", "高清", "默认", "艺术");
        List<String> categories = Arrays.asList("模板", "搞笑", "电商", "动漫", "素材", "海报");
        pictureTagCategory.setTags(tags);
        pictureTagCategory.setCategories(categories);
        return ResultUtils.success(pictureTagCategory);
    }

    @GetMapping("/download")
    public BaseResponse<String> downloadImage(@RequestParam Long id, HttpServletRequest request) throws MalformedURLException {
        // 通过 URL 读取远程图片
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "参数错误");
        return ResultUtils.success(pictureService.downloadPicture(id, request));
    }
}
