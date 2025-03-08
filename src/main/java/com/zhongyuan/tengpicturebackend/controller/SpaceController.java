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
import com.zhongyuan.tengpicturebackend.model.vo.SpaceLevel;
import com.zhongyuan.tengpicturebackend.model.vo.SpaceUserVO;
import com.zhongyuan.tengpicturebackend.model.vo.SpaceVO;
import com.zhongyuan.tengpicturebackend.model.vo.UserVo;
import com.zhongyuan.tengpicturebackend.service.SpaceService;
import com.zhongyuan.tengpicturebackend.service.SpaceUserService;
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
public class SpaceController {
    @Resource
    SpaceService spaceService;

    @Resource
    UserService userService;


    @Resource
    SpaceUserService spaceUserService;


    /**
     * 更新空间 管理员
     *
     * @param spaceUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        Space space = spaceUpdateRequest.toObj();
        spaceService.validSpace(space,false);
        Long picId = space.getId();
        // 必须存在
        Space oldPic = spaceService.getById(picId);
        ThrowUtils.throwIf(oldPic == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        //TODO 如果空间等级修改 需要回填参数这个部分有待考究
        spaceService.fillSpaceLevel(space);

        boolean result = spaceService.updateById(space);
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
    public BaseResponse<Space> getSpaceById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(space);
    }


    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpacePage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = spaceQueryRequest.getCurrent();
        int size = spaceQueryRequest.getPageSize();
        LambdaQueryWrapper<Space> queryWrapper = spaceService.getQueryWrapper(spaceQueryRequest);
        Page<Space> page = spaceService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(page);
    }

    // 删除空间
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(idRequest == null || idRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 必须存在
        Space oldSpace = spaceService.getById(idRequest.getId());
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        // 本人或者管理员
        User loginUser = userService.getLoginUser(request);

        spaceService.checkOwnerOrAdmin(loginUser, oldSpace);
        // 数据库删除
        boolean result = spaceService.removeById(idRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.NOT_FOUND_ERROR, "删除空间失败");
        return ResultUtils.success(true);
    }

    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long l = spaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(l);
    }

    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values()) // 获取所有枚举
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()))
                .collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }


    /**
     * 获取Vo
     *
     * @param id      空间id
     * @param request 请求
     * @return 空间vo
     */
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVoById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Space space = spaceService.lambdaQuery().eq(Space::getId, id).one();
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        UserVo userVo = UserVo.obj2Vo(userService.getLoginUser(request));
        Long loginUserId = userService.getLoginUser(request).getId();
        // 如果我是当前空间的成员，我就能访问这个空间
        LambdaQueryWrapper<SpaceUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SpaceUser::getSpaceId, space.getId());
        lambdaQueryWrapper.eq(SpaceUser::getUserId, loginUserId);
        boolean exists = spaceUserService.exists(lambdaQueryWrapper);
        // 非空间成员 无法访问
        boolean owner = space.getUserId().equals(loginUserId);

        ThrowUtils.throwIf(!owner&&!exists, ErrorCode.NO_AUTH_ERROR);
        return ResultUtils.success(SpaceVO.objToVo(space, userVo));
    }

    /**
     * 分页查询空间VO
     *
     * @param spaceQueryRequest 查询请求
     * @param request             请求
     * @return 空间vo分页
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVoPage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = spaceQueryRequest.getCurrent();
        int size = spaceQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(current < 0 || size > 20, ErrorCode.PARAMS_ERROR);
        //设置查询条件一定是审核完毕的
        LambdaQueryWrapper<Space> queryWrapper = spaceService.getQueryWrapper(spaceQueryRequest);

        Page<Space> page = spaceService.page(new Page<>(current, size), queryWrapper);
        Page<SpaceVO> spaceVoPage = new Page<>(current, size, page.getTotal());
        spaceVoPage.setRecords(spaceService.toVoList(page.getRecords(), request));
        return ResultUtils.success(spaceVoPage);
    }

    /**
     * 编辑空间
     *
     * @param spaceEditRequest 编辑空间参数
     * @param request            请求
     * @return 结果
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editeSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceEditRequest == null, ErrorCode.PARAMS_ERROR);
        Space space = spaceEditRequest.toObj();
        space.setEditTime(new Date());
        spaceService.validSpace(space,false);
        Long picId = space.getId();
        // 必须存在
        Space oldSpace = spaceService.getById(picId);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        // 本人或者管理员
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
        // 数据库操作
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
    @GetMapping("/type/{id}")
    public BaseResponse<Integer> getSpaceType(@PathVariable Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id==null||id <= 0, ErrorCode.PARAMS_ERROR);
        Integer spaceType = spaceService.getSpaceTypeById(id);
        return  ResultUtils.success(spaceType);
    }
}
