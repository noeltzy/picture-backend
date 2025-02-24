package com.zhongyuan.tengpicturebackend.manager.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor

/*
   梳理流程：
   接受message消息：
   ENTER_EDIT & EXIT_EDIT 不需要管editAction 通知给全部人，返回EDIT_INFO
   EXIT_EDIT 需要管editAction message中添加editAction->可以全添加
 */
public class PictureEditRequestMessage {


    /**
     * 消息类型，例如 "ENTER_EDIT", "EXIT_EDIT", "EDIT_ACTION"
     */
    private String type;

    /**
     * 执行的编辑动作
     */
    private String editAction;
}
