package com.zhongyuan.tengpicturebackend.manager.websocket.config;


import com.zhongyuan.tengpicturebackend.manager.websocket.WsHandshakeInterceptor;
import com.zhongyuan.tengpicturebackend.manager.websocket.WsPictureEditHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;

@Configuration
@EnableWebSocket
public class WsConfig implements WebSocketConfigurer {
    @Resource
    WsHandshakeInterceptor wsHandshakeInterceptor;
    @Resource
    private WsPictureEditHandler wsPictureEditHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(wsPictureEditHandler, "/ws/PictureEdit")
                .addInterceptors(wsHandshakeInterceptor)
                .setAllowedOrigins("*");

    }
}
