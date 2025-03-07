package com.zhongyuan.tengpicturebackend.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongyuan.tengpicturebackend.annotation.AuthCheck;
import com.zhongyuan.tengpicturebackend.common.BaseResponse;
import com.zhongyuan.tengpicturebackend.common.IdRequest;
import com.zhongyuan.tengpicturebackend.common.ResultUtils;
import com.zhongyuan.tengpicturebackend.constant.UserConstant;
import com.zhongyuan.tengpicturebackend.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.exception.ThrowUtils;
import com.zhongyuan.tengpicturebackend.model.dto.space.SpaceAddRequest;
import com.zhongyuan.tengpicturebackend.model.dto.space.SpaceEditRequest;
import com.zhongyuan.tengpicturebackend.model.dto.space.SpaceQueryRequest;
import com.zhongyuan.tengpicturebackend.model.dto.space.SpaceUpdateRequest;
import com.zhongyuan.tengpicturebackend.model.entity.Space;
import com.zhongyuan.tengpicturebackend.model.entity.SpaceUser;
import com.zhongyuan.tengpicturebackend.model.entity.User;
import com.zhongyuan.tengpicturebackend.model.enums.SpaceLevelEnum;
import com.zhongyuan.tengpicturebackend.model.vo.*;
import com.zhongyuan.tengpicturebackend.service.SpaceService;
import com.zhongyuan.tengpicturebackend.service.SpaceUserService;
import com.zhongyuan.tengpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/space")
@Slf4j
public class SpaceUserController {
    @Resource
    private UserService userService;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private SpaceService spaceService;
   //1.空间添加成员


    //2.空间移除成员

    //3.查询当前空间的成员
    @PostMapping("/list/members/{spaceId}")
    public BaseResponse<List<SpaceMemberVo>> listMembers(@PathVariable Long spaceId,HttpServletRequest request) {
        ThrowUtils.throwIf(spaceId==null,ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        //我是否是这个团队的成员
        LambdaQueryWrapper<SpaceUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SpaceUser::getSpaceId, spaceId);
        queryWrapper.eq(SpaceUser::getUserId,loginUser.getId());
        boolean exists = spaceUserService.exists(queryWrapper);
        ThrowUtils.throwIf(!exists,ErrorCode.PARAMS_ERROR,"非空间成员");
        return ResultUtils.success(spaceUserService.listSpaceMemberVo(spaceId));
    }


    //5.查询我加入的团队空间
    @PostMapping("/list/my")
    public BaseResponse<List<SpaceUserVO>> listMyTeamSpace(HttpServletRequest request) {
        //确保当前用户登录
        User user = userService.getLoginUser(request);
        Long userId = user.getId();
        LambdaQueryWrapper<SpaceUser> spaceUserQueryWrapper = new LambdaQueryWrapper<>();
        spaceUserQueryWrapper.eq(SpaceUser::getUserId, userId);
        List<SpaceUserVO> spaceUserVOList = spaceUserService.list(spaceUserQueryWrapper).stream().map(
                spaceUser -> {
                    //根据spaceID查询space
                    Space space = spaceService.getById(spaceUser.getSpaceId());
                    SpaceUserVO spaceUserVO = new SpaceUserVO();
                    BeanUtils.copyProperties(spaceUser, spaceUserVO);
                    spaceUserVO.setUser(UserVo.obj2Vo(user));
                    spaceUserVO.setSpace(SpaceVO.objToVo(space,UserVo.obj2Vo(user)));
                    return spaceUserVO;
                }
        ).collect(Collectors.toList());
        return ResultUtils.success(spaceUserVOList);
    }
}
