package com.zhongyuan.tengpicturebackend.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestLimit {
    /**
     *
     * @return 业务key 比如设置UploadPicture那么代表的是最后生成的Key就是xxx:UploadPicture:UserID
     */
    String key();  // 业务 key

    /**
     * @return  每秒准许访问的请求次数 默认 2
     */
    int times()  default  2;   // 每秒允许的请求次数
    int duration() default  10;
}
