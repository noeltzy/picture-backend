package com.zhongyuan.tengpicturebackend.pictureSpace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.user.UserLoginRequest;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.user.UserQueryRequest;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.user.UserRegisterRequest;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.vo.LoginUserVo;

import javax.servlet.http.HttpServletRequest;

/**
* @author Windows11
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-12-19 12:35:55
*/
public interface UserService extends IService<User> {

    long userRegister(UserRegisterRequest registerRequest);

    String encryptUserPassword(String userPassword);


    User getLoginUser(HttpServletRequest request);

    void userLogout(HttpServletRequest request);

    LoginUserVo userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);

    LambdaQueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    boolean isAdmin(User loginUser);

    User getLoginUserOrNoLogin(HttpServletRequest request);

}
