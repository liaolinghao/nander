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
package wang.bigbird.domain.framework.server.web.core.support.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import wang.bigbird.domain.framework.server.web.core.config.property.WebProperties;
import wang.bigbird.domain.framework.server.web.core.support.annotation.Encrypt;
import wang.bigbird.domain.framework.server.web.core.support.handler.SecurityHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 用于对controller层返回值为@ResponseBody类型的数据进行整体加密增强处理。
 * <p>
 * 有两种类型的处理器会将返回值作为ResponseBody
 * 1、返回值为HttpEntity
 * 2、加了@ResponseBody或@RestController注解
 * <p>
 *
 * @author Bigbird
 */
@Slf4j
@ControllerAdvice
public class EncryptAdvice implements ResponseBodyAdvice {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private WebProperties webProperties;
    @Autowired
    private SecurityHandler securityHandler;

    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        return methodParameter.hasMethodAnnotation(ResponseBody.class) && methodParameter.hasMethodAnnotation(Encrypt.class);
    }

    @Override
    public Object beforeBodyWrite(Object returnVal, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (returnVal == null) {
            return null;
        }
        HttpMessageConverter converter = (HttpMessageConverter) applicationContext.getBean(aClass);
        if (!converter.canWrite(returnVal.getClass(), mediaType)) {
            return returnVal;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            converter.write(returnVal, mediaType, new HttpOutputMessage() {
                @Override
                public OutputStream getBody() {
                    return output;
                }

                @Override
                public HttpHeaders getHeaders() {
                    return serverHttpResponse.getHeaders();
                }
            });
        } catch (IOException e) {
            log.error("", e);
        }
        String outStr = output.toString();
        String res;
        if (webProperties.getEnableEncrypt()) {
            res = securityHandler.encrypt(outStr);
        } else {
            res = outStr;
        }
        if (converter instanceof StringHttpMessageConverter) {
            serverHttpResponse.getHeaders().setContentLength(res.getBytes().length);
        }
        serverHttpResponse.getHeaders().setContentType(MediaType.TEXT_HTML);
        return res;
    }

}
