package com.zhongyuan.tengpicturebackend.pictureSpace.manager.websocket;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.Picture;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.Space;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.entity.User;
import com.zhongyuan.tengpicturebackend.pictureSpace.model.enums.SpaceTypeEnum;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.PictureService;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.SpaceService;
import com.zhongyuan.tengpicturebackend.pictureSpace.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@Component
@Slf4j
public class WsHandshakeInterceptor implements HandshakeInterceptor {
    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;


    /**
     * 建立WebSocket连接前需要校验
     *
     * @param request    请求
     * @param response   响应
     * @param wsHandler  处理器
     * @param attributes 设置绘画属性 给Ws session
     * @return 是否pass
     * @throws Exception 可能的异常
     */

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 获取当前用户
        log.info("try to connect to websocket");
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            // 拿到固定的参数 这里是String类型
            String pictureId = servletRequest.getParameter("pictureId");


            if (StrUtil.isBlank(pictureId)) {
                log.error("缺少图片ID参数拒绝握手");
                return false;
            }
            //获取当前用户
            User loginUser = userService.getLoginUser(servletRequest);
            System.out.println(loginUser);
            if (ObjUtil.isEmpty(loginUser)) {
                log.error("用户未登录拒绝连接");
                return false;
            }
            //如果无图片
            Picture picture = pictureService.getById(pictureId);
            if (ObjUtil.isEmpty(picture)) {
                log.error("图片不存在拒绝连接");
                return false;
            }
            Long spaceId = picture.getSpaceId();
            //查空间
            Space space;
            //非公共空间
            if (spaceId != null) {
                space = spaceService.getById(spaceId);
                if (ObjUtil.isEmpty(space)) {
                    log.error("图片空间不存在,拒绝");
                    return false;
                }
                if (space.getSpaceType() != SpaceTypeEnum.TEAM.getValue()) {
                    log.error("非团队空间,拒绝");
                    return false;
                }
            }

            // todo 校验当前用户是否有编辑权限
            // 设置登录用户信息等属性到WebSocket会话中
            attributes.put("user", loginUser);
            attributes.put("userId", loginUser.getId());
            attributes.put("pictureId", Long.valueOf(pictureId));
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
