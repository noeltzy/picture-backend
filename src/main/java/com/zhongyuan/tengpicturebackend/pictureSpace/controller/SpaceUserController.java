package com.zhongyuan.tengpicturebackend.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongyuan.tengpicturebackend.common.BaseResponse;
import com.zhongyuan.tengpicturebackend.common.ResultUtils;
import com.zhongyuan.tengpicturebackend.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.exception.ThrowUtils;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.spaceUser.SpaceUserAddRequest;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.Space;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.SpaceUser;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.User;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.enums.SpaceRoleEnum;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.vo.SpaceMemberVo;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.vo.SpaceUserVO;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.vo.SpaceVO;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.vo.UserVo;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.SpaceService;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.SpaceUserService;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    @PostMapping("/add/user")
    public BaseResponse<Boolean> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest, HttpServletRequest request) {
        if (spaceUserAddRequest == null || spaceUserAddRequest.getSpaceId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断权限
        User loginUser = userService.getLoginUser(request);
        Space space = spaceService.getById(spaceUserAddRequest.getSpaceId());
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()), ErrorCode.OPERATION_ERROR);
        SpaceUser spaceUser = new SpaceUser();
        //查看是否有
        Long userId = spaceUserAddRequest.getUserId();
        User byId = userService.getById(userId);
        ThrowUtils.throwIf(byId==null,ErrorCode.PARAMS_ERROR,"用户不存在");
        //查是否已经在空间中了
        LambdaQueryWrapper<SpaceUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SpaceUser::getUserId, byId.getId());
        queryWrapper.eq(SpaceUser::getSpaceId, spaceUserAddRequest.getSpaceId());
        boolean exists = spaceUserService.exists(queryWrapper);

        ThrowUtils.throwIf(exists,ErrorCode.PARAMS_ERROR,"用户已经在空间中");
        //查空间

        BeanUtils.copyProperties(spaceUserAddRequest, spaceUser);
        spaceUser.setSpaceRole(SpaceRoleEnum.VIEWER.getValue());
        boolean save = spaceUserService.save(spaceUser);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(true);
    }
    //2.空间移除成员

    //3.查询当前空间的成员
    @PostMapping("/list/members/{spaceId}")
    public BaseResponse<List<SpaceMemberVo>> listMembers(@PathVariable Long spaceId, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        //我是否是这个团队的成员
        LambdaQueryWrapper<SpaceUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SpaceUser::getSpaceId, spaceId);
        queryWrapper.eq(SpaceUser::getUserId, loginUser.getId());
        boolean exists = spaceUserService.exists(queryWrapper);
        ThrowUtils.throwIf(!exists, ErrorCode.PARAMS_ERROR, "非空间成员");
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
                    spaceUserVO.setSpace(SpaceVO.objToVo(space, UserVo.obj2Vo(user)));
                    return spaceUserVO;
                }
        ).collect(Collectors.toList());
        return ResultUtils.success(spaceUserVOList);
    }
}
