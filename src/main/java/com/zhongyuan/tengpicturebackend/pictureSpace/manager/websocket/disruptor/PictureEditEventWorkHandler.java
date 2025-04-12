package com.zhongyuan.tengpicturebackend.pictureSpace.manager.websocket.disruptor;

import com.lmax.disruptor.WorkHandler;
import com.zhongyuan.tengpicturebackend.pictureSpace.manager.websocket.WsPictureEditHandler;
import com.zhongyuan.tengpicturebackend.pictureSpace.manager.websocket.model.PictureEditMessageTypeEnum;
import com.zhongyuan.tengpicturebackend.pictureSpace.manager.websocket.model.PictureEditRequestMessage;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;

@Slf4j
@Component
public class PictureEditEventWorkHandler implements WorkHandler<PictureEditEvent> {

    @Resource
    @Lazy
    private WsPictureEditHandler pictureEditHandler;

    @Resource
    private UserService userService;

    @Override
    public void onEvent(PictureEditEvent event) throws Exception {
        //取到消息
        PictureEditRequestMessage pictureEditRequestMessage =event.getPictureEditRequestMessage();
        WebSocketSession session =event.getSession();
        //转化成枚举类,分类处理
        PictureEditMessageTypeEnum messageType =
                PictureEditMessageTypeEnum.getEnumByValue(pictureEditRequestMessage.getType());
        log.info(messageType.name());
        switch (messageType) {
            case ENTER_EDIT:
                pictureEditHandler.handleEnterEdit(pictureEditRequestMessage,session);
                break;
            case EXIT_EDIT:
                pictureEditHandler.handleExitEdit(pictureEditRequestMessage,session);
                break;
            case EDIT_ACTION:
                pictureEditHandler.handleOnEdit(pictureEditRequestMessage,session);
                break;
            default:
                pictureEditHandler.seedErrorMessage(session,"操作类型错误");
        }

    }
}
