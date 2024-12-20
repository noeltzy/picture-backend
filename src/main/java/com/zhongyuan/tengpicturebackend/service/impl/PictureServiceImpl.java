package com.zhongyuan.tengpicturebackend.service.impl;

import java.util.*;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongyuan.tengpicturebackend.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.exception.ThrowUtils;
import com.zhongyuan.tengpicturebackend.manager.FileManager;
import com.zhongyuan.tengpicturebackend.model.dto.file.PictureUploadResult;
import com.zhongyuan.tengpicturebackend.model.dto.picture.PictureQueryRequest;
import com.zhongyuan.tengpicturebackend.model.dto.picture.PictureUploadRequest;
import com.zhongyuan.tengpicturebackend.model.entity.Picture;
import com.zhongyuan.tengpicturebackend.model.entity.User;
import com.zhongyuan.tengpicturebackend.model.vo.PictureVo;
import com.zhongyuan.tengpicturebackend.model.vo.UserVo;
import com.zhongyuan.tengpicturebackend.service.PictureService;
import com.zhongyuan.tengpicturebackend.mapper.PictureMapper;
import com.zhongyuan.tengpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    FileManager fileManager;
    @Resource
    UserService userService;

    @Override
    public PictureVo uploadPicture(MultipartFile multipartFile, PictureUploadRequest uploadRequest, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //判断是上传还是更新
        Long picId = uploadRequest.getId();
        // 如果是更新，id需要存在
        if (picId != null) {
            boolean exists = this.lambdaQuery().eq(Picture::getId, picId).exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        //正常上传图片
        String filePrefix = String.format("public/%s", loginUser.getId());
        PictureUploadResult pictureUploadResult = fileManager.uploadPicture(multipartFile, filePrefix);
        Picture picture = PictureUploadResult.toPicture(pictureUploadResult, loginUser.getId());
        // 更新图片 需要添加其他字段
        if (picId != null) {
            picture.setId(picId);
            picture.setEditTime(new Date());
        }
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
        LambdaQueryWrapper<Picture> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(CollUtil.isNotEmpty(tags)){
            for (String tag : tags) {
                lambdaQueryWrapper.like(Picture::getTags,String.format("\"%s\"",tag));
            }

        }
        if(StrUtil.isNotBlank(searchText)){
            lambdaQueryWrapper.and(wrapper -> wrapper.like(Picture::getName,searchText)
                    .or()
                    .like(Picture::getIntroduction,searchText));
        }
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(userId),Picture::getUserId,userId);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(id),Picture::getId,id);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picWidth),Picture::getPicWidth,picWidth);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picHeight),Picture::getPicHeight,picHeight);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picScale),Picture::getPicScale,picScale);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picSize),Picture::getPicSize,picSize);
        lambdaQueryWrapper.like(ObjUtil.isNotEmpty(picFormat),Picture::getPicFormat,picFormat);
        lambdaQueryWrapper.eq(StrUtil.isNotBlank(category),Picture::getCategory,category);
        lambdaQueryWrapper.like(StrUtil.isNotBlank(introduction),Picture::getIntroduction,introduction);
        lambdaQueryWrapper.like(StrUtil.isNotBlank(name),Picture::getName,name);
        return lambdaQueryWrapper;
    }

    @Override
    public List<PictureVo> toVoList(List<Picture> records, HttpServletRequest request) {
        if(CollUtil.isEmpty(records)){
            return Collections.emptyList();
        }
        Set<Long> userIds = records.stream().map(Picture::getUserId).collect(Collectors.toSet());
        List<User> users = userService.listByIds(userIds);
        Map<Long, UserVo> userMap = users.stream().collect(Collectors.toMap(User::getId,UserVo::obj2Vo));
        return records.stream().map(
                picture -> PictureVo.obj2Vo(picture, userMap.get(picture.getUserId())
                )).collect(Collectors.toList());
    }
}




