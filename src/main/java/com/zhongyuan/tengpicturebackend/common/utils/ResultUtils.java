package com.zhongyuan.tengpicturebackend.common.utils;

import com.zhongyuan.tengpicturebackend.common.model.BaseResponse;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.ErrorCode;

import java.io.Serializable;

/**
 * 返回工具类
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public class ResultUtils implements Serializable {

    /**
     * 成功
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }
    public static <T> BaseResponse<T> success() {
        return new BaseResponse<>(0, null, "ok");
    }


    /**
     * 失败
     *
     * @param errorCode
     * @return
     */
    public static <T>BaseResponse <T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     *
     * @param code
     * @param message
     * @return
     */
    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    /**
     * 失败
     *
     * @param errorCode ErrorCode
     * @return
     */
    public static  <T> BaseResponse<T> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }
}
