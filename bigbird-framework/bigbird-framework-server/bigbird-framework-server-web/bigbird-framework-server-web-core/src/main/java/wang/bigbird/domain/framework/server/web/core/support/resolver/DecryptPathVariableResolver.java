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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;
import wang.bigbird.domain.framework.core.base.util.ObjectUtils;
import wang.bigbird.domain.framework.server.web.core.config.property.WebProperties;
import wang.bigbird.domain.framework.server.web.core.support.annotation.DecryptPathVariable;
import wang.bigbird.domain.framework.server.web.core.support.handler.SecurityHandler;

import java.util.Map;

/**
 * @author Bigbird
 */
@Component
public class DecryptPathVariableResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private WebProperties webProperties;
    @Autowired
    private SecurityHandler securityHandler;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(DecryptPathVariable.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        DecryptPathVariable decryptPathVariable = parameter.getParameterAnnotation(DecryptPathVariable.class);
        String paramName = StringUtils.isBlank(decryptPathVariable.value()) ? parameter.getParameterName() : decryptPathVariable.value();
        Map<String, String> uriVariables = (Map<String, String>) webRequest.getAttribute(
                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST
        );
        String encryptedValue = uriVariables.get(paramName);
        if (encryptedValue == null) {
            if (decryptPathVariable.required()) {
                throw new IllegalArgumentException("Required parameter '" + paramName + "' is not present.");
            }
            return null;
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
