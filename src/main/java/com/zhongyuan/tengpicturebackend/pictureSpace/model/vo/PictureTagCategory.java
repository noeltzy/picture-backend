package com.zhongyuan.tengpicturebackend.pictureSpace.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureTagCategory implements Serializable {
    private static final long serialVersionUID = -1736006149698513711L;
    List<String> tags;
    List<String> categories;
}
