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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 目标 WS 服务客户端（透传用）
 *
 * @author Bigbird
 */
@Slf4j
public class TargetWsClient extends WebSocketClient {

    /**
     * 最大缓存条数，防止内存溢出
     */
    private static final int MAX_PENDING = 300;

    /**
     * 与前端客户端的会话（用于把目标服务的消息回传给前端）
     */
    private final Session clientSession;
    /**
     * 与前端客户端的会话（用于把目标服务的消息回传给前端）
     */
    private final WebSocketSession webSocketSession;
    /**
     * 标记下游ws是否握手完成
     */
    private final AtomicBoolean connected = new AtomicBoolean(false);
    /**
     * 未就绪时缓存下游返回的应答消息
     */
    private final BlockingQueue<String> pendingMsgQueue = new LinkedBlockingQueue<>(MAX_PENDING);


    public TargetWsClient(URI serverUri, Session clientSession, WebSocketSession webSocketSession) {
        super(serverUri);
        this.clientSession = clientSession;
        this.webSocketSession = webSocketSession;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.info("Successfully connected to target WS service: {}", getURI());
        connected.set(true);
        // 连接成功，批量发送缓存的应答消息
        flushPendingMessages();
    }

    /**
     * 收到目标服务的消息 → 透传给前端客户端
     */
    @Override
    public void onMessage(String message) {
        // 连接未就绪：先放入缓存队列
        if (!connected.get()) {
            if (pendingMsgQueue.size() < MAX_PENDING) {
                pendingMsgQueue.offer(message);
                log.warn("Target ws not ready, cache msg, queueSize:{}", pendingMsgQueue.size());
            } else {
                log.error("Pending message queue full, drop msg:{}", message);
            }
            return;
        }
        // 连接就绪，直接透传
        relayMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("Disconnected from target WS service: {}", reason);
        connected.set(false);
        pendingMsgQueue.clear();
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
        connected.set(false);
        pendingMsgQueue.clear();
    }

    /**
     * 批量冲刷缓存消息
     */
    private void flushPendingMessages() {
        while (!pendingMsgQueue.isEmpty()) {
            String msg = pendingMsgQueue.poll();
            relayMessage(msg);
        }
    }

    /**
     * 透传目标服务响应消息给前端客户端
     *
     * @param message
     */
    private void relayMessage(String message) {
        boolean relayed = false;
        try {
            if (clientSession != null && clientSession.isOpen()) {
                clientSession.getBasicRemote().sendText(message);
                relayed = true;
            }
            if (webSocketSession != null && webSocketSession.isOpen()) {
                webSocketSession.sendMessage(new TextMessage(message));
                relayed = true;
            }
        } catch (Exception e) {
            log.error("Failed to relay response to client: {}", e.getMessage(), e);
            return;
        }
        if (relayed) {
            log.info("Target service response -> client relay successful: {}", message);
        } else {
            log.warn("All client session closed, discard target service response:{}", message);
        }
    }

}
