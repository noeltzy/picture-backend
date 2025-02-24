package com.zhongyuan.tengpicturebackend.service;

import com.zhongyuan.tengpicturebackend.common.IdRequest;
import com.zhongyuan.tengpicturebackend.model.dto.space.SpaceAddRequest;
import com.zhongyuan.tengpicturebackend.model.dto.spaceUser.SpaceUserAddRequest;
import com.zhongyuan.tengpicturebackend.model.dto.spaceUser.SpaceUserEditRequest;
import com.zhongyuan.tengpicturebackend.model.dto.spaceUser.SpaceUserQueryRequest;
import com.zhongyuan.tengpicturebackend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhongyuan.tengpicturebackend.model.entity.User;
import com.zhongyuan.tengpicturebackend.model.vo.SpaceUserVO;

import java.util.List;

/**
* @author Windows11
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2025-02-20 13:34:16
*/
public interface SpaceUserService extends IService<SpaceUser> {
    /**
     * 加入空间 只能空间管理员
     * @param spaceUserAddRequest 请求dto
     * @param loginUser 当前操作用户
     * @return 加入空间的id
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest, User loginUser);

    /**
     * 移除用户 只能空间管理员
     * @param idRequest 用户id
     * @param loginUser 当前操作用户
     * @return 结果
     */
    long removeUserFromSpace(IdRequest idRequest, User loginUser);

    /**
     * 查询空间内用户 只能空间管理员
     * @param spaceUserQueryRequest 请求dto
     * @param loginUser 当前操作用户
     * @return 结果list
     */
    List<SpaceUserVO> listSpaceUserVo(SpaceUserQueryRequest spaceUserQueryRequest, User loginUser);

    /**
     * 编辑空间内成员权限 只能空间管理员
     * @param spaceUserEditRequest 编辑请求dto
     * @param loginUser 当前操作用户
     * @return 是否操作成功
     */
    boolean editSpaceUser(SpaceUserEditRequest spaceUserEditRequest, User loginUser);
}
