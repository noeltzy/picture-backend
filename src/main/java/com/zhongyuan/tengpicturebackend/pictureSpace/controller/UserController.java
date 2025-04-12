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
import com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.user.*;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.User;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.vo.LoginUserVo;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.vo.UserVo;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 注册参数
     * @return 新用户id
     */
    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        Long userId = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(userId);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 登录参数
     * @param request          请求
     * @return loginUserVo
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVo> login(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        LoginUserVo loginUserVo = userService.userLogin(userLoginRequest, request);
        return ResultUtils.success(loginUserVo);
    }

    /**
     * 获取登录用户脱敏
     *
     * @param request 请求体
     * @return 用户vo
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVo> getLoginUserVO(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(LoginUserVo.convert(user));
    }

    /**
     * 用户注销
     *
     * @param request 请求体
     * @return 结果
     */
    @PostMapping("/logout")
    public BaseResponse<?> userLogout(HttpServletRequest request) {
        userService.userLogout(request);
        return ResultUtils.success();
    }

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        //设置默认密码
        final String DEFAULT_PASSWORD = "123456";
        String encryptPassword = userService.encryptUserPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "系统错误,添加失败");
        return ResultUtils.success(user.getId());
    }
    /**
     *  根据id获取对象
     * @param id 对象的id
     * @return 对象
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    @GetMapping("/get/vo")
    public BaseResponse<UserVo> getUserVoById(long id) {
        BaseResponse<User> bUser = getUserById(id);
        return ResultUtils.success(UserVo.obj2Vo(bUser.getData()));
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody IdRequest idRequest) {
        ThrowUtils.throwIf(idRequest == null || idRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean result = userService.removeById(idRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.NOT_FOUND_ERROR, "删除用户失败");
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null , ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVo>> listUserVoPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null , ErrorCode.PARAMS_ERROR);
        int current = userQueryRequest.getCurrent();
        int size = userQueryRequest.getPageSize();
        LambdaQueryWrapper<User> queryWrapper= userService.getQueryWrapper(userQueryRequest);
        Page<User> page = userService.page(new Page<>(current, size), queryWrapper);
        Page<UserVo> userPage = new Page<>(current,size,page.getTotal());
        userPage.setRecords(page.getRecords().stream().map(UserVo::obj2Vo).collect(Collectors.toList()));
        return ResultUtils.success(userPage);
    }




}
