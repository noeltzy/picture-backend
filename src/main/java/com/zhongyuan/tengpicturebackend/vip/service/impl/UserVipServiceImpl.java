package com.zhongyuan.tengpicturebackend.vip.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.pictureSpace.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.ImageGenTask;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.User;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.UserService;
import com.zhongyuan.tengpicturebackend.vip.model.entity.UserVip;
import com.zhongyuan.tengpicturebackend.vip.model.entity.VipOrder;
import com.zhongyuan.tengpicturebackend.vip.model.entity.dto.CreateVipOrderRequest;
import com.zhongyuan.tengpicturebackend.vip.model.entity.vo.UserVipVO;
import com.zhongyuan.tengpicturebackend.vip.model.enums.VipLevelEnum;
import com.zhongyuan.tengpicturebackend.vip.service.UserVipService;
import com.zhongyuan.tengpicturebackend.vip.mapper.UserVipMapper;
import com.zhongyuan.tengpicturebackend.vip.service.VipOrderService;
import com.zhongyuan.tengpicturebackend.vip.utils.VipOrderUtils;
import com.zhongyuan.tengpicturebackend.vip.utils.VipUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Date;

/**
* @author Windows11
* @description 针对表【user_vip(用户VIP表)】的数据库操作Service实现
* @createDate 2025-04-02 20:37:13
*/
@Service
@Slf4j
public class UserVipServiceImpl extends ServiceImpl<UserVipMapper, UserVip>
    implements UserVipService{




    @Resource
    @Lazy
    VipOrderService vipOrderService;

    @Resource
    TransactionTemplate transactionTemplate;

    @Override
    public UserVipVO upgradeUserVipLevel(User loginUser, int level) {
        //查找当前用户vip等级
        Long id = loginUser.getId();
        UserVip userVip = baseMapper.selectByUserId(id);
        // 说明没有开过VIP 设置 一个初始化vip
        if(userVip==null){
            //如果当前没VIP 默认插入一条免费一个月的VIP
            UserVip free = VipUtils.createUserVip(id, VipLevelEnum.FREE);
            //插入
            save(free);
            return UserVipVO.convert(free);
        }
        return null;
    }


    @Override
    public UserVip getByUserId(Long id) {
        UserVip userVip = baseMapper.selectByUserId(id);
        if(userVip==null){
            userVip  = VipUtils.createUserVip(id, VipLevelEnum.FREE);
            VipOrder order = VipOrderUtils.createOrder(id, VipLevelEnum.FREE);
            order.setPayStatus(VipOrderUtils.PAY_STATUS_PAID);
            order.setRemark("默认本月免费额度发放，开通免费VIP");
            UserVip finalUserVip = userVip;
            transactionTemplate.execute(status -> {
                vipOrderService.save(order);
                finalUserVip.setOrderNo(order.getOrderNo());
                boolean save = save(finalUserVip);
                return  null;
            });
            Date now = DateUtil.date();
            log.info("用户本月开通免费额度,userID :{},time:{}",id,now);
        }
        return userVip;
    }

    @Override
    public void useTokens(Long userVipId, int tokenRequired) {
        int lines = baseMapper.useToken(userVipId, tokenRequired);
        if(lines==0){
            log.info("用户:{},使用token:{},发生异常{}",userVipId,tokenRequired,ErrorCode.OPERATION_LIMIT.getMessage());
            // 这里是在用户创建调用的时候执行，为了防止超买，是用乐观锁，降级返回方案：
            throw new BusinessException(ErrorCode.OPERATION_LIMIT);
        }
    }

    /**
     * 退还 机制,如果任务失败，补偿任务执行消耗的Token
     * 乐观锁机制，如果更新函失败，没有一行更新，说明 需要上层事物回滚
     *
     * @param task
     */
    @Override
    public void tokenRefund(ImageGenTask task) {
        Long userVipId = task.getUserVipId();
        Integer tokensUsed = task.getTokensUsed();
        Integer tokenRefunded = task.getTokenRefunded();
        if(ObjUtil.isNotNull(tokenRefunded)&&tokenRefunded==0){
            int line = baseMapper.refundToken(userVipId, tokensUsed);
            if(line==0){
                log.info("task:{},任务失败 恢复 token:{},发生异常{}",userVipId,tokensUsed,ErrorCode.OPERATION_ERROR.getMessage());
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
            log.info("task:{},任务失败 恢复 token:{},成功",userVipId,tokensUsed);
        }
    }

    @Override
    public void cancelVip(UserVip oldVip) {
        baseMapper.cancelVipByVipId(oldVip.getId());
    }


}




