package com.zhongyuan.tengpicturebackend.model.dto.space;

import com.zhongyuan.tengpicturebackend.model.entity.Space;
import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceEditRequest implements Serializable {

    /**
     * 空间 id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    private static final long serialVersionUID = 1L;

    public Space toObj() {
        Space space = new Space();
        space.setId(id);
        space.setSpaceName(spaceName);
        return space;
    }
}
