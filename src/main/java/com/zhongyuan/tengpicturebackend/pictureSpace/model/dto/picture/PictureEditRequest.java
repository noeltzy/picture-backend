package com.zhongyuan.tengpicturebackend.pictureSpace.model.dto.picture;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.Picture;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureEditRequest implements Serializable {
    private static final long serialVersionUID = -8053523483390009402L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 图片名称
     */
    private String name;
    /**
     * 简介
     */
    private String introduction;
    /**
     * 分类
     */
    private String category;



    /**
     * 标签（JSON 数组）
     */
    private List<String> tags;

    public Picture toObj(){
        Picture picture = new Picture();
        picture.setId(id);
        picture.setName(name);
        picture.setIntroduction(introduction);
        picture.setCategory(category);
        picture.setTags(JSONUtil.toJsonStr(tags));
        return  picture;
    }
}
