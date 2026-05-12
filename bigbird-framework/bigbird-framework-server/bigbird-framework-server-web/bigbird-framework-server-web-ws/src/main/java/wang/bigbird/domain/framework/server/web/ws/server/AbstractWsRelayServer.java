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
package wang.bigbird.domain.framework.server.web.ws.server;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.springframework.beans.factory.annotation.Autowired;
import wang.bigbird.domain.framework.server.web.ws.base.constant.WsConstants;
import wang.bigbird.domain.framework.server.web.ws.config.property.WsProperties;
import wang.bigbird.domain.framework.server.web.ws.service.base.IDataProcessService;
import wang.bigbird.domain.framework.server.web.ws.service.base.ITargetWsAuthService;
import wang.bigbird.domain.framework.server.web.ws.service.base.ITokenService;
import wang.bigbird.domain.framework.server.web.ws.support.client.TargetWsClient;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用 WS 透传服务
 * 客户端请求：ws://ip:port/xx/xxx → 透传：ws://targetIp:targetPort/xx/xxx
 * 具体WS服务通过继承AbstractWsRelayServer实现
 *
 * @author Bigbird
 */
@Slf4j
public abstract class AbstractWsRelayServer {

    private static WsProperties wsProperties;
    private static ITokenService tokenService;
    private static ITargetWsAuthService targetWsAuthService;
    private static IDataProcessService dataProcessService;

    @Autowired
    public void setWsProperties(WsProperties wsProperties) {
        AbstractWsRelayServer.wsProperties = wsProperties;
    }

    @Autowired(required = false)
    public void setTokenService(ITokenService tokenService) {
        AbstractWsRelayServer.tokenService = tokenService;
    }

    @Autowired(required = false)
    public void setTargetWsAuthService(ITargetWsAuthService targetWsAuthService) {
        AbstractWsRelayServer.targetWsAuthService = targetWsAuthService;
    }

    @Autowired(required = false)
    public void setDataProcessService(IDataProcessService dataProcessService) {
        AbstractWsRelayServer.dataProcessService = dataProcessService;
    }

    /**
     * 会话映射：前端Session → 目标WS客户端
     */
    private static final ConcurrentHashMap<Session, WebSocketClient> CLIENT_MAP = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("path") String path) {
        // 1. 从握手信息中获取
        String appKey = (String) session.getUserProperties().get(WsConstants.APPKEY_PARAM_CODE);
        String token = (String) session.getUserProperties().get(WsConstants.TOKEN_PARAM_CODE);
        String requestPath = (String) session.getUserProperties().get(WsConstants.PATH_PARAM_CODE);
        log.info("New connection: path={}", requestPath);
        // 2. 鉴权
        if (tokenService != null && !tokenService.verifyToken(appKey, token)) {
            closeSession(session, "Authentication failed");
            return;
        }
        log.info("Authentication passed: appKey={}", appKey);
        // 3. 拼接目标地址（核心通用逻辑）
        String targetUrl = wsProperties.getTarget() + requestPath;
        // 4. 连接目标服务
        try {
            TargetWsClient targetClient = new TargetWsClient(new URI(targetUrl), session, null);
            if (targetWsAuthService != null) {
                targetWsAuthService.addAuthHeaders(targetClient);
            }
            targetClient.connect();
            CLIENT_MAP.put(session, targetClient);
            log.info("Relay established: {} → {}", requestPath, targetUrl);
        } catch (Exception e) {
            log.error("Failed to connect to target WS service: {}", e.getMessage(), e);
            closeSession(session, "Target service unavailable");
        }
    }

    /**
     * 消息透传：前端 → 目标WS
     *
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        WebSocketClient client = CLIENT_MAP.get(session);
        if (client == null || !client.isOpen()) {
            return;
        }
        try {
            String appKey = (String) session.getUserProperties().get(WsConstants.APPKEY_PARAM_CODE);
            String token = (String) session.getUserProperties().get(WsConstants.TOKEN_PARAM_CODE);
            String msg = message;
            if (dataProcessService != null) {
                msg = dataProcessService.processData(appKey, token, msg);
            }
            client.send(msg);
            log.info("Relaying message: {}", message);
        } catch (Exception e) {
            log.error("Failed to send message: {}", e.getMessage(), e);
        }
    }

    @OnClose
    public void onClose(Session session) {
        WebSocketClient client = CLIENT_MAP.remove(session);
        if (client != null && client.isOpen()) {
            client.close();
        }
        log.info("Connection closed, resources cleaned up");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("Service exception: {}", error.getMessage(), error);
    }

    private void closeSession(Session session, String reason) {
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, reason));
        } catch (Exception e) {
        }
    }

}
