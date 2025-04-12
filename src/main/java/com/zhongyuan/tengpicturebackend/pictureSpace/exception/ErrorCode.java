package com.zhongyuan.tengpicturebackend.pictureSpace.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NO_TOKEN_QUOTA_ERROR(50021, "无Token额度"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    // 降级写法,如果发生乐观锁失败现象,直接返回降级结果,您的操作太频繁,请稍后再试
    OPERATION_LIMIT(50000, "操作太频繁,请稍后再试"),
    OPERATION_ERROR(50001, "操作失败"),
    TOO_MANY_REQUESTS_ERROR(42900, "请求过于频繁，请稍后再试");
    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}