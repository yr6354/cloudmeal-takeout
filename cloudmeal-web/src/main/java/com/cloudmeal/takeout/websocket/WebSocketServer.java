package com.cloudmeal.takeout.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 服务端：管理商家端会话连接。
 *
 * <p>会话容器由 {@code HashMap} 改为 {@link ConcurrentHashMap}：WebSocket 回调由容器线程触发，
 * 多客户端连接/断开时对 HashMap 的并发读写存在死循环与数据丢失风险。</p>
 */
@Component
@ServerEndpoint("/ws/{sid}")
@Slf4j
public class WebSocketServer {

    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        SESSIONS.put(sid, session);
        log.info("WebSocket 连接建立：sid={}，当前在线 {} 个", sid, SESSIONS.size());
    }

    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        log.debug("收到 WebSocket 消息：sid={}, message={}", sid, message);
    }

    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        SESSIONS.remove(sid);
        log.info("WebSocket 连接断开：sid={}，当前在线 {} 个", sid, SESSIONS.size());
    }

    /** 群发消息给所有已连接的商家端 */
    public void sendToAllClient(String message) {
        SESSIONS.forEach((sid, session) -> {
            if (!session.isOpen()) {
                SESSIONS.remove(sid);
                return;
            }
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException ex) {
                log.warn("WebSocket 推送失败：sid={}, cause={}", sid, ex.getMessage());
            }
        });
    }
}
