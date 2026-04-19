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
package wang.bigbird.domain.framework.server.web.core.support.resolver;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import wang.bigbird.domain.framework.core.base.util.ObjectUtils;
import wang.bigbird.domain.framework.server.web.core.config.property.WebProperties;
import wang.bigbird.domain.framework.server.web.core.support.annotation.DecryptRequestParam;
import wang.bigbird.domain.framework.server.web.core.support.handler.SecurityHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Bigbird
 */
@Component
public class DecryptRequestParamResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private WebProperties webProperties;
    @Autowired
    private SecurityHandler securityHandler;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(DecryptRequestParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        DecryptRequestParam decryptRequestParam = parameter.getParameterAnnotation(DecryptRequestParam.class);
        String paramName = StringUtils.isBlank(decryptRequestParam.value()) ? parameter.getParameterName() : decryptRequestParam.value();
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String encryptedValue = request.getParameter(paramName);
        if (encryptedValue == null) {
            if (decryptRequestParam.required()) {
                throw new IllegalArgumentException("Required parameter '" + paramName + "' is not present.");
            }
            return decryptRequestParam.defaultValue();
        }
        String decryptedValue;
        if (webProperties.getEnableDecrypt()) {
            decryptedValue = securityHandler.decrypt(encryptedValue);
        } else {
            decryptedValue = encryptedValue;
        }
        return ObjectUtils.convertType(decryptedValue, parameter.getParameterType());
    }

}
