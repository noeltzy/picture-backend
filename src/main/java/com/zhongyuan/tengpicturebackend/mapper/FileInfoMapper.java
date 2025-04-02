package com.zhongyuan.tengpicturebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhongyuan.tengpicturebackend.model.entity.FileInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
* @author Windows11
* @description 针对表【file_info】的数据库操作Mapper
* @createDate 2025-04-02 13:04:33
* @Entity com.zhongyuan.tengpicturebackend.model.entity.FileInfo
*/
public interface FileInfoMapper extends BaseMapper<FileInfo> {

    @Select("SELECT uploadCount FROM file_info WHERE fileHash = #{fileHash}")
    int getUploadCountByHash(@Param("fileHash") String fileHash);
}




