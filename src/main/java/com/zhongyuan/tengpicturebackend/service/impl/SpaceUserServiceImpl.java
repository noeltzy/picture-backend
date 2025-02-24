package com.zhongyuan.tengpicturebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongyuan.tengpicturebackend.common.IdRequest;
import com.zhongyuan.tengpicturebackend.exception.BusinessException;
import com.zhongyuan.tengpicturebackend.exception.ErrorCode;
import com.zhongyuan.tengpicturebackend.exception.ThrowUtils;
import com.zhongyuan.tengpicturebackend.model.dto.space.SpaceAddRequest;
import com.zhongyuan.tengpicturebackend.model.dto.spaceUser.SpaceUserAddRequest;
import com.zhongyuan.tengpicturebackend.model.dto.spaceUser.SpaceUserEditRequest;
import com.zhongyuan.tengpicturebackend.model.dto.spaceUser.SpaceUserQueryRequest;
import com.zhongyuan.tengpicturebackend.model.entity.SpaceUser;
import com.zhongyuan.tengpicturebackend.model.entity.User;
import com.zhongyuan.tengpicturebackend.model.enums.SpaceRoleEnum;
import com.zhongyuan.tengpicturebackend.model.vo.SpaceUserVO;
import com.zhongyuan.tengpicturebackend.service.SpaceUserService;
import com.zhongyuan.tengpicturebackend.mapper.SpaceUserMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author Windows11
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
 * @createDate 2025-02-20 13:34:16
 */
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
        implements SpaceUserService {

    @Override
    public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest, User loginUser) {
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = spaceUserAddRequest.getSpaceId();
        Long userId = spaceUserAddRequest.getUserId();
        String spaceRole = spaceUserAddRequest.getSpaceRole();
        // 权限校验
        if(!SpaceRoleEnum.ADMIN.getValue().equals(spaceRole)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 用户是否已经加入
        this.isUserInSpace(userId,spaceId);
        //加入
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setSpaceId(spaceId);
        spaceUser.setUserId(userId);
        spaceUser.setSpaceRole(spaceRole);
        this.save(spaceUser);
        return spaceUser.getId();
    }

    private void getUserSpaceRole(Long userId,Long spaceId) {
        LambdaQueryWrapper<SpaceUser> spaceUserQueryWrapper = new LambdaQueryWrapper<>();
        spaceUserQueryWrapper.select(SpaceUser::getSpaceRole)
                .eq(SpaceUser::getId, userId)
                .eq(SpaceUser::getSpaceId, spaceId);
    }

    @Override
    public long removeUserFromSpace(IdRequest idRequest, User loginUser) {
        ThrowUtils.throwIf(idRequest == null, ErrorCode.PARAMS_ERROR);

        return 0;
    }

    @Override
    public List<SpaceUserVO> listSpaceUserVo(SpaceUserQueryRequest spaceUserQueryRequest, User loginUser) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);

        return Collections.emptyList();
    }

    @Override
    public boolean editSpaceUser(SpaceUserEditRequest spaceUserEditRequest, User loginUser) {
        ThrowUtils.throwIf(spaceUserEditRequest == null, ErrorCode.PARAMS_ERROR);

        return false;
    }
    private void isUserInSpace(Long userId, Long spaceId) {
        boolean exists = this.lambdaQuery().eq(SpaceUser::getUserId, userId).eq(SpaceUser::getSpaceId, spaceId).exists();
        ThrowUtils.throwIf(exists, ErrorCode.PARAMS_ERROR,"重复加入");
    }
}




