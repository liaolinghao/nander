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
package wang.bigbird.domain.framework.server.web.core.support.interceptor;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.AbstractClientHttpResponse;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import wang.bigbird.domain.framework.common.crypto.service.base.ICryptoService;
import wang.bigbird.domain.framework.core.base.tool.Coder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Rest请求加密解密拦截器
 * 基于Spring Cloud的微服务在使用RestTemplate调用的时候，
 * 可通过注入该拦截器实现对请求数据的自动加密解密处理。
 *
 * @author Bigbird
 */
@Setter
@Slf4j
public class CryptoRestInterceptor implements ClientHttpRequestInterceptor {

    /**
     * 加密请求数据
     */
    ICryptoService encryptService;
    String encryptKey;

    /**
     * 解密响应数据
     */
    ICryptoService decryptService;
    String decryptKey;

    /**
     * 偏移量
     */
    String iv;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        byte[] encryptBody;
        if (encryptService != null && encryptKey != null && body != null && body.length > 0) {
            encryptBody = encryptService.encrypt2String(new String(body), encryptKey, iv).getBytes(Coder.DEFAULT_ENCODING);
        } else {
            encryptBody = body;
        }
        Assert.notNull(encryptBody, "encryptBody must not be null");
        ClientHttpResponse response = execution.execute(request, encryptBody);
        if (decryptService != null && decryptKey != null) {
            InputStream oldInput = response.getBody();
            String str = new String(StreamUtils.copyToByteArray(oldInput));
            byte[] decryptData = decryptService.decrypt2String(str, decryptKey, iv).getBytes(Coder.DEFAULT_ENCODING);
            ByteArrayInputStream input = new ByteArrayInputStream(decryptData);
            return new CryptoClientHttpResponse(response, input);
        }
        return response;
    }

    class CryptoClientHttpResponse extends AbstractClientHttpResponse {

        CryptoClientHttpResponse(ClientHttpResponse response, InputStream input) {
            this.response = response;
            this.input = input;
        }

        ClientHttpResponse response;
        InputStream input;

        @Override
        public int getRawStatusCode() throws IOException {
            return response.getRawStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return response.getStatusText();
        }

        @Override
        public void close() {
            response.close();
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    log.error("Close:", e);
                }
            }
        }

        @Override
        public InputStream getBody() {
            return input;
        }

        @Override
        public HttpHeaders getHeaders() {
            return response.getHeaders();
        }
    }

}
