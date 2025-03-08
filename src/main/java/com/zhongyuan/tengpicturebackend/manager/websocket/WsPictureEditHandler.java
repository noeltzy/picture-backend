package com.zhongyuan.tengpicturebackend.manager.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.zhongyuan.tengpicturebackend.manager.websocket.disruptor.PictureEditEventProducer;
import com.zhongyuan.tengpicturebackend.manager.websocket.model.PictureEditActionEnum;
import com.zhongyuan.tengpicturebackend.manager.websocket.model.PictureEditMessageTypeEnum;
import com.zhongyuan.tengpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.zhongyuan.tengpicturebackend.manager.websocket.model.PictureEditResponseMessage;
import com.zhongyuan.tengpicturebackend.model.entity.User;
import com.zhongyuan.tengpicturebackend.model.vo.UserVo;
import com.zhongyuan.tengpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WsPictureEditHandler extends TextWebSocketHandler {

    // 图片的编辑状态 key:pictureId,value:UserId
    private final Map<Long, Long> PICTURE_EDIT_USER = new ConcurrentHashMap<>();
    //保存全部的会话的集合 key: pictureId,value:WebSocketSession
    private final Map<Long, Set<WebSocketSession>> PICTURE_EDIT_SESSIONS = new ConcurrentHashMap<>();

    @Resource
    UserService userService;
    @Autowired
    private PictureEditEventProducer pictureEditEventProducer;

    /**
     * 建立连接需要做的事情
     * 1. 保存会话记录信息
     *
     * @param session 会话
     * @throws Exception 异常
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        //1.保存会话到集合
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        User user = (User) session.getAttributes().get("user");
        PICTURE_EDIT_SESSIONS.computeIfAbsent(pictureId, k -> ConcurrentHashMap.newKeySet()).add(session);
        //2.广播通知 构建消息体
        PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
        responseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        responseMessage.setMessage(String.format("用户%s : 进入编辑会话", user.getUserName()));
        responseMessage.setUser(UserVo.obj2Vo(user));
        // 第一次加入协同编辑 需要查看当前是否有成员正在编辑,根据这个来判断是否可以编辑
        if(PICTURE_EDIT_USER.containsKey(pictureId)) {
            User editor = userService.getById(PICTURE_EDIT_USER.get(pictureId));
            responseMessage.setEditUser(UserVo.obj2Vo(editor));

        }
        broadcastToAllPictureEdit(pictureId, responseMessage);
    }
    /**
     * @param session 会话
     * @param status  状态
     * @throws Exception 异常
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        //移除会话
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        User user = (User) session.getAttributes().get("user");
        //如果当前用户处于编辑状态，需要移除
        if(PICTURE_EDIT_USER.containsKey(pictureId)) {
            Long currentEditUserId = PICTURE_EDIT_USER.get(pictureId);
            if(currentEditUserId.equals(user.getId())) {
                PICTURE_EDIT_USER.remove(pictureId);
            }
        }
        // 移除会话
        Set<WebSocketSession> webSocketSessions = PICTURE_EDIT_SESSIONS.get(pictureId);
        if(webSocketSessions != null) {
            webSocketSessions.remove(session);
            //当前图片无人处于编辑状态，则直接移除
            if(webSocketSessions.isEmpty()) {
                PICTURE_EDIT_SESSIONS.remove(pictureId);
            }
        }
        //构造返回消息
        PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
        responseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
        responseMessage.setMessage(String.format("用户%s 退出编辑", user.getUserName()));
        responseMessage.setUser(UserVo.obj2Vo(user));
        broadcastToAllPictureEdit(pictureId, responseMessage);
    }

    /**
     * 消息需要做的事情
     *
     * @param session 会话
     * @param message 消息
     * @throws Exception 异常
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        //取到消息
        PictureEditRequestMessage pictureEditRequestMessage =
                JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        pictureEditEventProducer.publishEvent(pictureEditRequestMessage,session);
    }

    /**
     * 进入编辑状态
     * 1. 用户进入编辑状态的条件（权限在获取连接的时候已经确认,这个位置无需判断权限）
     *    当前没有用户正在编辑
     * @param pictureEditRequestMessage 具体的消息体
     * @param session 会话
     */
    public void handleEnterEdit(PictureEditRequestMessage pictureEditRequestMessage,
                                WebSocketSession session) throws IOException {
        //session中拿到公共参数
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        if (!PICTURE_EDIT_USER.containsKey(pictureId)) {
            // 无人编辑 那就加入编辑
            PICTURE_EDIT_USER.put(pictureId,user.getId() );
            //发送消息给其他人
            broadcastMessage(session,pictureEditRequestMessage);
        }else{

        }
    }

    /**
     * 正在编辑
     * 1. 校验用户是否能够编辑
     * 2. 校验当前用户是否是当前的编辑者
     * 3.
     * @param pictureEditRequestMessage 具体的消息体
     * @param session 会话
     */
    public void handleOnEdit(PictureEditRequestMessage pictureEditRequestMessage,
                             WebSocketSession session) throws IOException {
        //session中拿到公共参数
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        Long editingUserId = PICTURE_EDIT_USER.get(pictureId);
        PictureEditActionEnum editActionEnum =
                PictureEditActionEnum.getEnumByValue(pictureEditRequestMessage.getEditAction());
        log.info("1111");
        log.info(String.valueOf(editingUserId));
        log.info(String.valueOf(user.getId()));
        if(editActionEnum==null){
            log.error("无效编辑操作");
            this.seedErrorMessage(session,"无效编辑操作");
            return;
        }
        if(editingUserId!=null&&editingUserId.equals(user.getId())){
            //可以执行编辑
            log.info("可以编辑");
            this.broadcastMessage(session,pictureEditRequestMessage);
        }
    }

    /**
     * 退出编辑状态
     * @param pictureEditRequestMessage 具体的消息体
     * @param session 会话
     */
    public void handleExitEdit(PictureEditRequestMessage pictureEditRequestMessage,
                               WebSocketSession session) throws IOException {
        //session中拿到公共参数
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        Long editingUserId = PICTURE_EDIT_USER.get(pictureId);
        if(editingUserId!=null&&editingUserId.equals(user.getId())){
            //退出编辑状态
            log.info("退出!");
            PICTURE_EDIT_USER.remove(pictureId);
            this.broadcastMessage(session,pictureEditRequestMessage);
        }
        else{
            this.seedErrorMessage(session,"错误操作");
        }
    }





    public void seedErrorMessage(WebSocketSession session,String message){
        User user = (User) session.getAttributes().get("user");
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ERROR.getValue());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(UserVo.obj2Vo(user));
    }


    /**
     *      * 1 INFO类通知: xx进出编辑 需要广播给全部成员
     *      * 2 ON_EDITION: 需要
     * @param session
     * @param pictureEditRequestMessage
     * @throws IOException
     */
    private void broadcastMessage(WebSocketSession session, PictureEditRequestMessage pictureEditRequestMessage) throws IOException {

        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");

        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        PictureEditMessageTypeEnum messageTypeEnum = PictureEditMessageTypeEnum.getEnumByValue(pictureEditRequestMessage.getType());
        String editAction = pictureEditRequestMessage.getEditAction();
        pictureEditResponseMessage.setEditAction(editAction);
        pictureEditResponseMessage.setUser(UserVo.obj2Vo(user));
        pictureEditResponseMessage.setMessage(String.format("用户%s : %s %s", user.getUserName(), messageTypeEnum.getText(),editAction==null?" ":editAction));
        // 如果是进出类的消息发送给后端
        if(PictureEditMessageTypeEnum.ENTER_EDIT.equals(messageTypeEnum)||PictureEditMessageTypeEnum.EXIT_EDIT.equals(messageTypeEnum)){
            pictureEditResponseMessage.setType(messageTypeEnum.getValue());
            broadcastToAllPictureEdit(pictureId, pictureEditResponseMessage);
        }
        // 执行编辑操作
        else {
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            broadcastToAllPictureEdit(pictureId,pictureEditResponseMessage,session);
        }
    }
    /**
     * 广播消息给全部pictureId的编辑者
     *
     * @param pictureId       pictureId
     * @param responseMessage 需要返回的消息
     */
    private void broadcastToAllPictureEdit(Long pictureId, PictureEditResponseMessage responseMessage,
                                           WebSocketSession excludeSession) throws IOException {
        // 拿到全部的对话
        Set<WebSocketSession> webSocketSessions = PICTURE_EDIT_SESSIONS.get(pictureId);
        if (CollUtil.isNotEmpty(webSocketSessions)) {
            TextMessage textMessage = this.response2Message(responseMessage);
            // 广播到全部的方法
            for (WebSocketSession webSocketSession : webSocketSessions) {
                if (webSocketSession.equals(excludeSession)) {
                    continue;
                }
                if (webSocketSession.isOpen()) {
                    webSocketSession.sendMessage(textMessage);
                }
            }
        }

    }

    private void broadcastToAllPictureEdit(Long pictureId,
                                           PictureEditResponseMessage responseMessage) throws IOException {
        broadcastToAllPictureEdit(pictureId, responseMessage, null);
    }

    private TextMessage response2Message(Object responseMessage) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        mapper.registerModule(module);
        String message = mapper.writeValueAsString(responseMessage);
        return new TextMessage(message);
    }
}
