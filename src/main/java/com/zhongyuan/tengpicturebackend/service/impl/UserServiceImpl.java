package com.zhongyuan.tengpicturebackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongyuan.tengpicturebackend.constant.UserConstant;
import com.zhongyuan.tengpicturebackend.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.exception.ThrowUtils;
import com.zhongyuan.tengpicturebackend.mapper.UserMapper;
import com.zhongyuan.tengpicturebackend.model.dto.user.UserLoginRequest;
import com.zhongyuan.tengpicturebackend.model.dto.user.UserQueryRequest;
import com.zhongyuan.tengpicturebackend.model.dto.user.UserRegisterRequest;
import com.zhongyuan.tengpicturebackend.model.entity.User;
import com.zhongyuan.tengpicturebackend.model.enums.UserRoleEnum;
import com.zhongyuan.tengpicturebackend.model.vo.LoginUserVo;
import com.zhongyuan.tengpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Windows11
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2024-12-19 12:35:55
 */


@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Override
    public long userRegister(UserRegisterRequest registerRequest) {
        String userAccount = registerRequest.getUserAccount();
        String userPassword = registerRequest.getUserPassword();
        String checkPassword = registerRequest.getCheckPassword();

        //1.参数校验
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword), ErrorCode.PARAMS_ERROR);

        //长度限制
        ThrowUtils.throwIf(userAccount.length() < 6, ErrorCode.PARAMS_ERROR, "账号过短");
        ThrowUtils.throwIf(userPassword.length() < 6, ErrorCode.PARAMS_ERROR, "密码过短");
        //2.密码相等
        ThrowUtils.throwIf(!StrUtil.equals(userPassword, checkPassword), ErrorCode.PARAMS_ERROR, "两次密码不相等");
        //3.存在校验

        Long count = this.lambdaQuery().eq(User::getUserAccount, userAccount).count();
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "用户已存在");
        //4.密码加密
        String encryptUserPassword = encryptUserPassword(userPassword);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptUserPassword);
        user.setUserName("默认");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean result = this.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "系统错误,注册失败");
        //5.返回id
        return user.getId();
    }

    @Override
    public String encryptUserPassword(String userPassword) {
        String salt = "0x5xx51";
        return DigestUtils.md5DigestAsHex((userPassword + salt).getBytes());
    }

    /**
     * 系统内部获取当前登录用户，没登录返回null
     *
     * @param request 请求
     * @return User || null
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 查询缓存
        Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        User user = (User) attribute;
        if (user == null || user.getId() == null) {
            ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        }
        // 查询数据库 最求性能可以关闭
        user = this.getById(user.getId());
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        return user;
    }

    @Override
    public void userLogout(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        ThrowUtils.throwIf(attribute == null, ErrorCode.OPERATION_ERROR);
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATUS);
    }

    @Override
    public LoginUserVo userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // 基础校验
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(userAccount.length() < 6, ErrorCode.PARAMS_ERROR, "账号过短");
        ThrowUtils.throwIf(userPassword.length() < 6, ErrorCode.PARAMS_ERROR, "密码过短");
        //对比密码
        String encryptPassword = encryptUserPassword(userPassword);
        User user = this.lambdaQuery().eq(User::getUserAccount, userAccount).eq(User::getUserPassword, encryptPassword).one();
        if (user == null) {
            log.info("user:{} login fail", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码错误");
        }
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATUS, user);
        return LoginUserVo.convert(user);
    }

    @Override
    public LambdaQueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String phone = userQueryRequest.getPhone();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(userAccount),User::getUserAccount, userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName),User::getUserName, userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile),User::getUserProfile, userProfile);
        queryWrapper.eq(StrUtil.isNotBlank(userRole),User::getUserRole, userRole);
        queryWrapper.eq(id!=null, User::getId, id);
        queryWrapper.like(StrUtil.isNotBlank(phone),User::getPhone, phone);
        return queryWrapper;
    }

}




