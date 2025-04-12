package com.zhongyuan.tengpicturebackend.vip.service;

import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.ImageGenTask;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.User;
import com.zhongyuan.tengpicturebackend.vip.model.entity.UserVip;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhongyuan.tengpicturebackend.vip.model.entity.VipOrder;
import com.zhongyuan.tengpicturebackend.vip.model.entity.dto.CreateVipOrderRequest;
import com.zhongyuan.tengpicturebackend.vip.model.entity.vo.UserVipVO;

/**
* @author Windows11
* @description 针对表【user_vip(用户VIP表)】的数据库操作Service
* @createDate 2025-04-02 20:37:13
*/
public interface UserVipService extends IService<UserVip> {

    UserVipVO upgradeUserVipLevel(User loginUser, int level);

    UserVip getByUserId(Long id);

    void useTokens(Long userVipId, int tokenRequired);

    void tokenRefund(ImageGenTask task);


    void cancelVip(UserVip oldVip);
}
