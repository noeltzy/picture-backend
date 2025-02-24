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
import com.zhongyuan.tengpicturebackend.model.entity.Space;
import com.zhongyuan.tengpicturebackend.model.entity.User;
import com.zhongyuan.tengpicturebackend.model.enums.SpaceLevelEnum;
import com.zhongyuan.tengpicturebackend.model.vo.SpaceLevel;
import com.zhongyuan.tengpicturebackend.model.vo.SpaceVO;
import com.zhongyuan.tengpicturebackend.model.vo.UserVo;
import com.zhongyuan.tengpicturebackend.service.SpaceService;
import com.zhongyuan.tengpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
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
   //1.空间添加成员

    //2.空间移除成员

    //3.空间编辑成员权限

    //4.查询当前用户加入的空间

    //5.查询当前空间的成员

}
