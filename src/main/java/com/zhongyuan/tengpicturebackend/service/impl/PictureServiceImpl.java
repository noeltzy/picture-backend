package com.zhongyuan.tengpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongyuan.tengpicturebackend.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.exception.ThrowUtils;
import com.zhongyuan.tengpicturebackend.manager.upload.FilePictureUpload;
import com.zhongyuan.tengpicturebackend.manager.upload.PictureUploadTemplate;
import com.zhongyuan.tengpicturebackend.manager.upload.UrlPictureUpload;
import com.zhongyuan.tengpicturebackend.mapper.PictureMapper;
import com.zhongyuan.tengpicturebackend.model.dto.file.PictureUploadResult;
import com.zhongyuan.tengpicturebackend.model.dto.picture.PictureQueryRequest;
import com.zhongyuan.tengpicturebackend.model.dto.picture.PictureReviewRequest;
import com.zhongyuan.tengpicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.zhongyuan.tengpicturebackend.model.dto.picture.PictureUploadRequest;
import com.zhongyuan.tengpicturebackend.model.entity.Picture;
import com.zhongyuan.tengpicturebackend.model.entity.User;
import com.zhongyuan.tengpicturebackend.model.enums.PictureReviewStatusEnum;
import com.zhongyuan.tengpicturebackend.model.enums.UserRoleEnum;
import com.zhongyuan.tengpicturebackend.model.vo.PictureVo;
import com.zhongyuan.tengpicturebackend.model.vo.UserVo;
import com.zhongyuan.tengpicturebackend.service.PictureService;
import com.zhongyuan.tengpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Windows11
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2024-12-20 14:12:33
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    FilePictureUpload filePictureUpload;
    @Resource
    UrlPictureUpload urlPictureUpload;

    @Resource
    UserService userService;

    @Override
    public PictureVo uploadPicture(Object inputSource, PictureUploadRequest uploadRequest, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //判断是上传还是更新
        Long picId = uploadRequest.getId();
        // 如果是更新，id需要存在
        if (picId != null) {
            Picture oldPic = this.getById(picId);
            ThrowUtils.throwIf(oldPic == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            //本人或者管理员可继续更新图片
            if (!Objects.equals(oldPic.getUserId(), loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
        //正常上传图片
        String filePrefix = String.format("public/%s", loginUser.getId());
        PictureUploadTemplate fileManager = filePictureUpload;
        // 根据 inputSource 判断上传方式
        if (inputSource instanceof String) {
            fileManager = urlPictureUpload;
        }
        PictureUploadResult pictureUploadResult = fileManager.uploadPicture(inputSource, filePrefix);
        Picture picture = PictureUploadResult.toPicture(pictureUploadResult, loginUser.getId());

        // 仅限批量抓取图片更新：
        String batchFetchDefaultName = uploadRequest.getBatchFetchDefaultName();
        String batchFetchCategory = uploadRequest.getCategory();
        if(StrUtil.isNotBlank(batchFetchDefaultName)){
            picture.setName(batchFetchDefaultName);
        }
        if(StrUtil.isNotBlank(batchFetchCategory)){
            picture.setCategory(batchFetchCategory);
        }
        // end

        // 更新图片 需要添加其他字段
        if (picId != null) {
            picture.setId(picId);
            picture.setEditTime(new Date());
        }//end
        //更新审核参数
        this.setReviewParam(picture, loginUser);

        boolean res = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!res, ErrorCode.SYSTEM_ERROR, "保存失败");
        log.info("文件访问路径: {}", picture.getUrl());
        return PictureVo.obj2Vo(picture, UserVo.obj2Vo(loginUser));
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        ThrowUtils.throwIf(picture.getId() == null || picture.getId() <= 0, ErrorCode.PARAMS_ERROR);
        if (StrUtil.isNotBlank(picture.getUrl())) {
            ThrowUtils.throwIf(picture.getUrl().length() > 1024, ErrorCode.PARAMS_ERROR, "URL长度不能超过1024");
        }
    }

    @Override
    public LambdaQueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();

        LambdaQueryWrapper<Picture> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                lambdaQueryWrapper.like(Picture::getTags, String.format("\"%s\"", tag));
            }

        }
        if (StrUtil.isNotBlank(searchText)) {
            lambdaQueryWrapper.and(wrapper -> wrapper.like(Picture::getName, searchText)
                    .or()
                    .like(Picture::getIntroduction, searchText));
        }
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(userId), Picture::getUserId, userId);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(id), Picture::getId, id);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), Picture::getReviewerId, reviewerId);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picWidth), Picture::getPicWidth, picWidth);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picHeight), Picture::getPicHeight, picHeight);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picSize), Picture::getPicSize, picSize);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), Picture::getReviewStatus, reviewStatus);
        lambdaQueryWrapper.like(ObjUtil.isNotEmpty(picFormat), Picture::getPicFormat, picFormat);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picScale), Picture::getPicScale, picScale);
        lambdaQueryWrapper.eq(StrUtil.isNotBlank(category), Picture::getCategory, category);
        lambdaQueryWrapper.like(StrUtil.isNotBlank(introduction), Picture::getIntroduction, introduction);
        lambdaQueryWrapper.like(StrUtil.isNotBlank(name), Picture::getName, name);
        lambdaQueryWrapper.like(StrUtil.isNotBlank(reviewMessage), Picture::getReviewMessage, reviewMessage);
        lambdaQueryWrapper.orderByDesc(Picture::getEditTime);
        return lambdaQueryWrapper;
    }

    @Override
    public List<PictureVo> toVoList(List<Picture> records, HttpServletRequest request) {
        if (CollUtil.isEmpty(records)) {
            return Collections.emptyList();
        }
        Set<Long> userIds = records.stream().map(Picture::getUserId).collect(Collectors.toSet());
        List<User> users = userService.listByIds(userIds);
        Map<Long, UserVo> userMap = users.stream().collect(Collectors.toMap(User::getId, UserVo::obj2Vo));
        return records.stream().map(
                picture -> PictureVo.obj2Vo(picture, userMap.get(picture.getUserId())
                )).collect(Collectors.toList());
    }

    @Override
    public void reviewPicture(PictureReviewRequest pictureReviewRequest, User user) {
        //校验参数
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum statusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        ThrowUtils.throwIf(statusEnum == null || id == null, ErrorCode.PARAMS_ERROR);
        //判断图片是否存在
        Picture picture = getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        //判断当前用户是否为管理员
        ThrowUtils.throwIf(!UserRoleEnum.ADMIN.getValue().equals(user.getUserRole()), ErrorCode.NO_AUTH_ERROR);
        //审核状态不能重复
        ThrowUtils.throwIf(reviewStatus.equals(picture.getReviewStatus()) ||
                PictureReviewStatusEnum.REVIEWING.equals(statusEnum), ErrorCode.PARAMS_ERROR);
        //更新图片审核状态
        Picture pictureUpdate = new Picture();
        BeanUtil.copyProperties(pictureReviewRequest, pictureUpdate);
        //添加相关
        pictureUpdate.setReviewerId(user.getId());
        pictureUpdate.setReviewTime(new Date());
        boolean b = this.updateById(pictureUpdate);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public void setReviewParam(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewTime(new Date());
            picture.setReviewerId(loginUser.getId());
        } else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        // 参数校验
        String prefixName = pictureUploadByBatchRequest.getPrefixName();
        String searchText = pictureUploadByBatchRequest.getSearchText();
        String category = pictureUploadByBatchRequest.getCategory();

        if (StrUtil.isBlank(prefixName)) {
            prefixName = "默认" + searchText;
        }

        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(count == null || count > 30, ErrorCode.PARAMS_ERROR, "抓取数量过多");
        // 拼接搜索引擎URL
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        // 爬虫抓取图片网页
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片抓取失败");
        }
        // 获取图片链接
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "解析元素失败");
        }

        Elements imgElementList = div.select("img.mimg");
        int uploadCount = 0;
        if(imgElementList.isEmpty()){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "抓取失败");
        }
        for (Element imgElement : imgElementList) {
            String imgUrl = imgElement.attr("src");
            if (StrUtil.isBlank(imgUrl)) {
                log.info("当前连接为空,跳过,{}",fetchUrl);
                continue;
            }
            //处理文件地址,防止出现转义错误去除请求文件地址的查询参数
            int queryIndex = imgUrl.indexOf("?");
            if (queryIndex > -1) {
                imgUrl = imgUrl.substring(0, queryIndex);
            }
            // 批量上传
            try {
                PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                pictureUploadRequest.setBatchFetchDefaultName(prefixName+"-"+(uploadCount+1));
                if(StrUtil.isNotBlank(category)){
                    pictureUploadRequest.setCategory(category);
                }
                this.uploadPicture(imgUrl,pictureUploadRequest , loginUser);
                log.info("上传图片成功: {}", imgUrl);
                uploadCount++;
            } catch (Exception e) {
                log.error("上传图片失败: {}", imgUrl, e);
                continue;
            }
            // 早停
            if(uploadCount>=count){
                break;
            }
        }
        return uploadCount;
    }
}




