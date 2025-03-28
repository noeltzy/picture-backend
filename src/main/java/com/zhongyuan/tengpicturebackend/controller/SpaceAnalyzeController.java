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
import com.zhongyuan.tengpicturebackend.model.dto.space.SpaceAddRequest;
import com.zhongyuan.tengpicturebackend.model.dto.space.SpaceEditRequest;
import com.zhongyuan.tengpicturebackend.model.dto.space.SpaceQueryRequest;
import com.zhongyuan.tengpicturebackend.model.dto.space.SpaceUpdateRequest;
import com.zhongyuan.tengpicturebackend.model.dto.space.analyze.SpaceCategoryAnalyzeRequest;
import com.zhongyuan.tengpicturebackend.model.dto.space.analyze.SpaceTagAnalyzeRequest;
import com.zhongyuan.tengpicturebackend.model.dto.space.analyze.SpaceUsageAnalyzeRequest;
import com.zhongyuan.tengpicturebackend.model.dto.space.analyze.SpaceUserAnalyzeRequest;
import com.zhongyuan.tengpicturebackend.model.entity.Space;
import com.zhongyuan.tengpicturebackend.model.entity.User;
import com.zhongyuan.tengpicturebackend.model.vo.SpaceVO;
import com.zhongyuan.tengpicturebackend.model.vo.UserVo;
import com.zhongyuan.tengpicturebackend.model.vo.analyze.*;
import com.zhongyuan.tengpicturebackend.service.SpaceAnalyzeService;
import com.zhongyuan.tengpicturebackend.service.SpaceService;
import com.zhongyuan.tengpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/space/analyze")
@Slf4j
public class SpaceAnalyzeController {
    @Resource
    SpaceAnalyzeService spaceAnalyzeService;

    @Resource
    UserService userService;

    @PostMapping("/usage")
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = spaceAnalyzeService.getSpaceUsageAnalyzeResponse(spaceUsageAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceUsageAnalyzeResponse);
    }

    @PostMapping("/category")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse> > getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<SpaceCategoryAnalyzeResponse> spaceCategoryAnalyzeResponse = spaceAnalyzeService.getSpaceCategoryAnalyzeResponse(spaceCategoryAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceCategoryAnalyzeResponse);
    }

    @PostMapping("/tag")
    public BaseResponse<List<SpaceTagAnalyzeResponse> > getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<SpaceTagAnalyzeResponse> spaceTagAnalyzeResponse = spaceAnalyzeService.getSpaceTagAnalyzeResponse(spaceTagAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceTagAnalyzeResponse);
    }

    @PostMapping("/user")
    public BaseResponse<List<SpaceUserAnalyzeResponse> > getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<SpaceUserAnalyzeResponse> spaceUserAnalyzeResponse = spaceAnalyzeService.getSpaceUserAnalyzeResponse(spaceUserAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceUserAnalyzeResponse);
    }
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/rank")
    public BaseResponse<List<Space> > getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<Space> spaceRankAnalyzeResponse = spaceAnalyzeService.getSpaceRankAnalyzeResponse(spaceRankAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceRankAnalyzeResponse);
    }
}
