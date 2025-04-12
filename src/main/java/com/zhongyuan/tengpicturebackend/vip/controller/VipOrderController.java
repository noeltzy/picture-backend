package com.zhongyuan.tengpicturebackend.vip.controller;


import cn.hutool.core.util.ObjUtil;
import com.zhongyuan.tengpicturebackend.common.model.BaseResponse;
import com.zhongyuan.tengpicturebackend.common.utils.ResultUtils;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.ThrowUtils;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.User;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.UserService;
import com.zhongyuan.tengpicturebackend.vip.model.entity.VipOrder;
import com.zhongyuan.tengpicturebackend.vip.model.entity.dto.CreateVipOrderRequest;
import com.zhongyuan.tengpicturebackend.vip.model.entity.dto.PayVipOrderRequest;
import com.zhongyuan.tengpicturebackend.vip.service.VipOrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/order")
public class VipOrderController {

    @Resource
    UserService userService;

    @Resource
    VipOrderService vipOrderService;


    @PostMapping("/create/order")
    BaseResponse<VipOrder> createBuyVipOrder(@RequestBody CreateVipOrderRequest createVipOrderRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(ObjUtil.isNull(createVipOrderRequest), ErrorCode.PARAMS_ERROR);
        VipOrder vipOrder =vipOrderService.createBuyVipOrder(createVipOrderRequest,loginUser);
        return ResultUtils.success(vipOrder);
    }


    @PostMapping("/pay/order")
    BaseResponse<String> payBuyVipOrder(@RequestBody PayVipOrderRequest payVipOrderRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(ObjUtil.isNull(payVipOrderRequest), ErrorCode.PARAMS_ERROR);
        String order =vipOrderService.payBuyVipOrder(payVipOrderRequest,loginUser);
        return ResultUtils.success(order);
    }
}
