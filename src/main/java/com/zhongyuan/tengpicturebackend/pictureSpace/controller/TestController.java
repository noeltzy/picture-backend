package com.zhongyuan.tengpicturebackend.pictureSpace.controller;

import com.zhongyuan.tengpicturebackend.common.annotation.RequestLimit;
import com.zhongyuan.tengpicturebackend.common.model.BaseResponse;
import com.zhongyuan.tengpicturebackend.common.utils.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("test")
public class TestController {

    @RequestLimit(key = "test",times = 1)
    @GetMapping("/testLimit")
    public BaseResponse<String> testLimit() {
        return ResultUtils.success("请求通过");
    }
}
