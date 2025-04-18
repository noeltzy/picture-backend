package com.zhongyuan.tengpicturebackend.pictureSpace.mapper;

import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.SpaceUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.vo.SpaceMemberVo;

import java.util.List;

/**
* @author Windows11
* @description 针对表【space_user(空间用户关联)】的数据库操作Mapper
* @createDate 2025-02-20 13:34:16
* @Entity com.zhongyuan.tengpicturebackend.model.entity.SpaceUser
*/
public interface SpaceUserMapper extends BaseMapper<SpaceUser> {

     List <SpaceMemberVo>     getSpaceMembers(Long spaceId);

}




