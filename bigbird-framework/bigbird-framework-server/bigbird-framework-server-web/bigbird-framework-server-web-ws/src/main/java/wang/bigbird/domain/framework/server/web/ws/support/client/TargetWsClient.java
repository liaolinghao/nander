/*
 * Copyright (c) 2026 廖凌浩 / 鸟域
 *
 * Licensed under the Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package wang.bigbird.domain.framework.server.web.ws.support.client;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.websocket.Session;
import java.net.URI;

/**
 * 目标 WS 服务客户端（透传用）
 *
 * @author Bigbird
 */
@Slf4j
public class TargetWsClient extends WebSocketClient {

    /**
     * 与前端客户端的会话（用于把目标服务的消息回传给前端）
     */
    private final Session clientSession;
    /**
     * 与前端客户端的会话（用于把目标服务的消息回传给前端）
     */
    private final WebSocketSession webSocketSession;

    public TargetWsClient(URI serverUri, Session clientSession, WebSocketSession webSocketSession) {
        super(serverUri);
        this.clientSession = clientSession;
        this.webSocketSession = webSocketSession;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.info("Successfully connected to target WS service: {}", getURI());
    }

    /**
     * 收到目标服务的消息 → 透传给前端客户端
     */
    @Override
    public void onMessage(String message) {
        try {
            if (clientSession != null && clientSession.isOpen()) {
                clientSession.getBasicRemote().sendText(message);
                log.info("Target service response -> client relay successful: {}", message);
            }
            if (webSocketSession != null && webSocketSession.isOpen()) {
                webSocketSession.sendMessage(new TextMessage(message));
                log.info("Target service response -> client relay successful: {}", message);
            }
        } catch (Exception e) {
            log.error("Failed to relay response to client: {}", e.getMessage(), e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("Disconnected from target WS service: {}", reason);
        try {
            if (webSocketSession != null && webSocketSession.isOpen()) {
                webSocketSession.close();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onError(Exception e) {
        log.error("Target WS client exception: {}", e.getMessage(), e);
    }

}
