package com.zhongyuan.tengpicturebackend.mapper;

import com.zhongyuan.tengpicturebackend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhongyuan.tengpicturebackend.model.vo.SpaceMemberVo;

import java.util.List;
import java.util.Map;

/**
* @author Windows11
* @description 针对表【space_user(空间用户关联)】的数据库操作Mapper
* @createDate 2025-02-20 13:34:16
* @Entity com.zhongyuan.tengpicturebackend.model.entity.SpaceUser
*/
public interface SpaceUserMapper extends BaseMapper<SpaceUser> {

     List <SpaceMemberVo> getSpaceMembers(Long spaceId);

}




