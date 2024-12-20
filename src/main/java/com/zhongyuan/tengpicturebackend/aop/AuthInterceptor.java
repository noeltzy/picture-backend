package com.zhongyuan.tengpicturebackend.aop;


import com.zhongyuan.tengpicturebackend.annotation.AuthCheck;
import com.zhongyuan.tengpicturebackend.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.exception.ThrowUtils;
import com.zhongyuan.tengpicturebackend.model.entity.User;
import com.zhongyuan.tengpicturebackend.model.enums.UserRoleEnum;
import com.zhongyuan.tengpicturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private UserService userService;

    /**
     * 权限校验
     * @param joinPoint 切入点
     * @param authCheck 注释
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ThrowUtils.throwIf(requestAttributes==null, ErrorCode.SYSTEM_ERROR);
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User user = userService.getLoginUser(request);
        // 无需权限放行
        if(mustRoleEnum ==null){
            return joinPoint.proceed();
        }
        //否则用户必须是某种权限才行
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(user.getUserRole());
        // 当前用户无权限
        ThrowUtils.throwIf(userRoleEnum==null, ErrorCode.NO_AUTH_ERROR);
        // 要求必须管理员权限,但是用户非管理员
        ThrowUtils.throwIf(UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum),
                ErrorCode.NO_AUTH_ERROR);

        return joinPoint.proceed();

    }
}
