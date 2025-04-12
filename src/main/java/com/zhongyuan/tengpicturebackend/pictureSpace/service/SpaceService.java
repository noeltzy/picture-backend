package com.zhongyuan.tengpicturebackend.pictureSpace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.space.SpaceAddRequest;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.space.SpaceQueryRequest;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.User;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.enums.SpaceRoleEnum;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Windows11
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-02-13 09:43:14
*/
public interface SpaceService extends IService<Space> {
    void validSpace(Space space, boolean add);

    LambdaQueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    List<SpaceVO> toVoList(List<Space> spaces, HttpServletRequest request);

    void fillSpaceLevel(Space space);

    long addSpace(SpaceAddRequest spaceAddRequest, HttpServletRequest request);

    void checkVolume(Space space);

    /**
     * @param spaceId 空间id
     * @param picSize 图片大小
     */
    void updateVolume(Long spaceId, Long picSize);

    void checkOwnerOrAdmin(User loginUser, Space oldSpace);

    /**
     * 查询spaceType
     *
     * @param id
     * @return
     */
    Integer getSpaceTypeById(Long id);

    void checkSpaceOptionAuth(Long spaceId, User loginUser, SpaceRoleEnum requestRole);

    void checkSpaceOptionAuth(Space space, User loginUser, SpaceRoleEnum requestRole);

    SpaceVO getSpaceVoById(long id, HttpServletRequest request);
}
