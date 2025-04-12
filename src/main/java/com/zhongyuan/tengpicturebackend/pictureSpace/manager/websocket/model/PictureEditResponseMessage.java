package com.zhongyuan.tengpicturebackend.manager.websocket.model;

import com.zhongyuan.tengpicturebackend.pictureSpace.model.vo.UserVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PictureEditResponseMessage {

    /**
     * 消息类型，例如 "INFO", "ERROR", "ENTER_EDIT", "EXIT_EDIT", "EDIT_ACTION"
     */
    private String type;

    /**
     * 信息
     */
    private String message;

    /**
     * 执行的编辑动作
     */
    private String editAction;

    /**
     * 用户信息
     */
    private UserVo user;

    /**
     * 如果没有正在编辑的用户，就不用描述了
     */
    private UserVo editUser;
}
