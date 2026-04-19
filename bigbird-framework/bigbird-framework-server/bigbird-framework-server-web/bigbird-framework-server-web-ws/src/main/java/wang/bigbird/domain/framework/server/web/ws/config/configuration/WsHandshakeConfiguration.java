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
package wang.bigbird.domain.framework.server.web.ws.config.configuration;

import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.core.base.util.url.UrlUtils;
import wang.bigbird.domain.framework.server.web.ws.base.constant.WsConstants;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * 握手配置器（读取请求中包含的认证信息：appKey + token）
 *
 * @author Bigbird
 */
@Slf4j
public class WsHandshakeConfiguration extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        // 获取请求头
        Map<String, List<String>> headers = request.getHeaders();
        URI uri = request.getRequestURI();
        String url = uri.toString();
        String requestPath = uri.getPath();
        String appKey = getHeaderValue(headers, WsConstants.APPKEY_HEADER_CODE);
        if (StringUtils.isBlank(appKey)) {
            appKey = UrlUtils.getParameter(url, WsConstants.APPKEY_PARAM_CODE, "");
        }
        String token = getHeaderValue(headers, WsConstants.TOKEN_HEADER_CODE);
        if (StringUtils.isBlank(token)) {
            token = UrlUtils.getParameter(url, WsConstants.TOKEN_PARAM_CODE, "");
        }
        // 存入会话，供 @OnOpen 使用
        sec.getUserProperties().put(WsConstants.APPKEY_PARAM_CODE, appKey);
        sec.getUserProperties().put(WsConstants.TOKEN_PARAM_CODE, token);
        sec.getUserProperties().put(WsConstants.PATH_PARAM_CODE, requestPath);
        log.info("Handshake: path={}, appKey={}, token={}", requestPath, appKey, token);
    }

    private String getHeaderValue(Map<String, List<String>> headers, String key) {
        List<String> values = headers.get(key);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }

}
