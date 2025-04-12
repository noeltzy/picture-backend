package com.zhongyuan.tengpicturebackend.controller;

import com.zhongyuan.tengpicturebackend.annotation.RequestLimit;
import com.zhongyuan.tengpicturebackend.common.BaseResponse;
import com.zhongyuan.tengpicturebackend.common.ResultUtils;
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
