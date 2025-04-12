package com.zhongyuan.tengpicturebackend.vip.controller;


import cn.hutool.core.util.ObjUtil;
import com.zhongyuan.tengpicturebackend.common.model.BaseResponse;
import com.zhongyuan.tengpicturebackend.common.utils.ResultUtils;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.ThrowUtils;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.User;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.UserService;
import com.zhongyuan.tengpicturebackend.vip.model.entity.UserVip;
import com.zhongyuan.tengpicturebackend.vip.model.entity.VipOrder;
import com.zhongyuan.tengpicturebackend.vip.model.entity.dto.CreateVipOrderRequest;
import com.zhongyuan.tengpicturebackend.vip.model.entity.vo.UserVipVO;
import com.zhongyuan.tengpicturebackend.vip.model.entity.vo.VipBenefitsVo;
import com.zhongyuan.tengpicturebackend.vip.service.UserVipService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/vip")
public class UserVipController {
    @Resource
    private UserVipService userVipService;
    @Resource
    UserService userService;





    @PostMapping("/new")
    BaseResponse<UserVipVO> newUserVip(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        UserVipVO userVipVO = userVipService.upgradeUserVipLevel(loginUser, 0);
        if(userVipVO == null) {
            return ResultUtils.success();
        }
        return ResultUtils.success(userVipVO);
    }

    @GetMapping("/my")
    BaseResponse<UserVipVO> getMyVip(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        UserVip userVip = userVipService.getByUserId(loginUser.getId());
        return ResultUtils.success(UserVipVO.convert(userVip));
    }

    /**
     * 获取所有VIP等级权益列表
     *
     * @return VIP权益列表
     */
    @GetMapping("/list/vipBenefits")
    public BaseResponse<List<VipBenefitsVo>> listVipBenefits() {
        // 直接从枚举转换获取所有VIP权益
        List<VipBenefitsVo> benefitsList = VipBenefitsVo.getAllBenefits();
        return ResultUtils.success(benefitsList);
    }
}
