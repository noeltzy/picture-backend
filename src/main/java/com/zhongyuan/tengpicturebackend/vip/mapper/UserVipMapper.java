package com.zhongyuan.tengpicturebackend.vip.mapper;

import com.zhongyuan.tengpicturebackend.vip.model.entity.UserVip;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
* @author Windows11
* @description 针对表【user_vip(用户VIP表)】的数据库操作Mapper
* @createDate 2025-04-02 20:37:13
* @Entity generator.domain.UserVip
*/
public interface UserVipMapper extends BaseMapper<UserVip> {

    @Select("SELECT * FROM user_vip " +
            "WHERE userId = #{userId} " +
            "AND status = 1 " +  // 1-生效中
            "AND endTime > NOW() " + // 未过期
            "ORDER BY id DESC " + // 如果有多条，取最新的
            "LIMIT 1")
    UserVip selectByUserId(@Param("userId") Long userId);

    @Update("UPDATE user_vip " +
            "SET usedTokens = usedTokens +  #{tokenRequired} " +
            "WHERE id = #{userVipId} " +
            "AND (usedTokens + #{tokenRequired} )<= totalTokens")  // 确保不超过总额度
    int useToken(@Param("userVipId")Long userVipId,@Param("tokenRequired") int tokenRequired);

    @Update("UPDATE user_vip " +
            "SET usedTokens = usedTokens - #{tokenRequired} " +
            "WHERE id = #{userVipId} ")
    int refundToken(@Param("userVipId")Long userVipId,@Param("tokenRequired") int tokenRequired);


    @Update("UPDATE user_vip " +
            "SET status = 0 " +
            "WHERE id = #{id}")
    void cancelVipByVipId(Long id);
}




