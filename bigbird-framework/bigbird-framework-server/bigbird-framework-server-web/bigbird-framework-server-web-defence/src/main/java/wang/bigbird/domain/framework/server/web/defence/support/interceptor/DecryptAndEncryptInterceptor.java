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
package wang.bigbird.domain.framework.server.web.defence.support.interceptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import wang.bigbird.domain.framework.common.crypto.service.base.IEnvelopeCryptoService;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.server.web.core.base.util.HttpUtils;
import wang.bigbird.domain.framework.server.web.core.support.filter.CachedHttpServletRequestWrapper;
import wang.bigbird.domain.framework.server.web.core.support.filter.CachedHttpServletResponseWrapper;
import wang.bigbird.domain.framework.server.web.defence.config.property.DefenceProperties;
import wang.bigbird.domain.framework.server.web.defence.domain.pojo.ApiSecurityItem;
import wang.bigbird.domain.framework.server.web.defence.domain.pojo.CallerItem;
import wang.bigbird.domain.framework.server.web.defence.service.cache.IApiSecurityCacheService;
import wang.bigbird.domain.framework.server.web.defence.support.security.AccessData;

import javax.annotation.Resource;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 对接口请求数据自动解密和响应数据自动加密的拦截器
 * <p>
 *
 * @author Bigbird
 */
@Slf4j
@Component
public class DecryptAndEncryptInterceptor extends AbstractInterceptor {

    @Resource
    private DefenceProperties defenceProperties;

    @Autowired
    private IApiSecurityCacheService apiSecurityCacheService;

    @Autowired
    private IEnvelopeCryptoService envelopeCryptoService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (isFormRequest(request)) {
            // form表单提交请求不做处理
            return true;
        }
        ApiSecurityItem securityItem = apiSecurityCacheService.getApiSecurityItem(HttpUtils.getRequestApi(handler, request.getRequestURI(), request.getContextPath()), request.getMethod());
        if (securityItem == null || !securityItem.getRequestDecryptEnable()) {
            // 接口不需要解密处理
            return true;
        }
        // 获取调用者信息
        AccessData accessData = loadAccessData(request, handler);
        CallerItem caller = getCaller(accessData);
        // 包装请求对象，以便可以修改加密请求参数
        // TODO form表单提交请求对应请求对象是ContentCachingRequestWrapper，后续需要处理，需要修改该对象中的数据
        CachedHttpServletRequestWrapper requestWrapper = (CachedHttpServletRequestWrapper) request;
        Set<String> keyFields = defenceProperties.getKeyFields();
        // 先解密普通请求参数
        Map<String, String[]> parameterMap = requestWrapper.getParameterMap();
        if (CollectionUtils.isNotEmpty(keyFields) && CollectionUtils.isNotEmpty(parameterMap)) {
            Map<String, String[]> decryptedMap = new HashMap<>(parameterMap.size());
            for (String key : parameterMap.keySet()) {
                String[] values = parameterMap.get(key);
                if (keyFields.contains(key)) {
                    // 解密字段
                    String[] decryptedValues = new String[values.length];
                    for (int i = 0; i < values.length; i++) {
                        decryptedValues[i] = envelopeCryptoService.decrypt2String(values[i], caller.getPrivateKey(), "");
                    }
                    decryptedMap.put(key, decryptedValues);
                } else {
                    decryptedMap.put(key, values);
                }
            }
            // 更新解密后的参数
            requestWrapper.setParameterMap(decryptedMap);
        }
        // 再解密路径参数
        Map<String, String> uriVariables = (Map<String, String>) request.getAttribute(
                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE
        );
        if (CollectionUtils.isNotEmpty(uriVariables) && CollectionUtils.isNotEmpty(keyFields)) {
            Map<String, String> decryptedVariables = new HashMap<>(uriVariables.size());
            boolean decrypted = false;
            for (Map.Entry<String, String> entry : uriVariables.entrySet()) {
                String paramName = entry.getKey();
                String paramValue = entry.getValue();
                if (keyFields.contains(paramName)) {
                    decryptedVariables.put(paramName, envelopeCryptoService.decrypt2String(paramValue, caller.getPrivateKey(), ""));
                    decrypted = true;
                } else {
                    decryptedVariables.put(paramName, paramValue);
                }
            }
            if (decrypted) {
                // 用解密后的参数替换原始参数
                request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, decryptedVariables);
                String uriTemplate = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
                String decryptedUri = buildDecryptedUri(uriTemplate, decryptedVariables);
                requestWrapper.setRequestURI(decryptedUri);
            }
        }
        // 再解密数据包参数
        String body = requestWrapper.getBody();
        if (StringUtils.isNotBlank(body)) {
            String decryptedBody;
            if (caller.getPackaged()) {
                // 整体级解密
                decryptedBody = envelopeCryptoService.decrypt2String(body, caller.getPrivateKey(), "");
            } else {
                if (CollectionUtils.isNotEmpty(keyFields)) {
                    // 字段级解密
                    JsonNode rootNode = JsonUtils.getMapper().readTree(body);
                    // 对@RequestBody中的加密字段自动解密
                    decryptFields(CommonConstants.EMPTY, rootNode, keyFields, caller.getPrivateKey());
                    decryptedBody = JsonUtils.getMapper().writeValueAsString(rootNode);
                } else {
                    decryptedBody = body;
                }
            }
            requestWrapper.setBody(decryptedBody);
        }
        return true;
    }

    /**
     * 正常流程（无异常）：DispatcherServlet → 拦截器 preHandle（返回 true）→ Controller → 拦截器 postHandle → 视图渲染 → 拦截器 afterCompletion → 响应
     * 异常流程（Controller 抛异常）：DispatcherServlet → 拦截器 preHandle（返回 true）→ Controller（抛异常）→ 跳过 postHandle → 统一异常处理器（@RestControllerAdvice）→ 拦截器 afterCompletion → 响应
     * 简单说：postHandle 的设计初衷是「对正常返回的请求做后续处理（如修改响应数据、视图优化）」，异常场景下它不会执行，但 afterCompletion 会执行（无论是否异常）。
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 从包装类中获取缓存的响应数据
        CachedHttpServletResponseWrapper wrapper = loadCachedHttpServletResponseWrapper(response);
        if (wrapper == null) {
            return;
        }
        String originalData = wrapper.getContent();
        if (isFormRequest(request)) {
            // form表单提交请求不做处理
            wrapper.write(originalData);
            return;
        }
        ApiSecurityItem securityItem = apiSecurityCacheService.getApiSecurityItem(HttpUtils.getRequestApi(handler, request.getRequestURI(), request.getContextPath()), request.getMethod());
        if (securityItem == null || !securityItem.getResponseEncryptEnable()) {
            // 接口不需要加密处理
            wrapper.write(originalData);
            return;
        }
        // 获取调用者信息
        AccessData accessData = loadAccessData(request, handler);
        CallerItem caller = getCaller(accessData);
        JsonNode rootNode = JsonUtils.getMapper().readTree(originalData);
        if (caller.getPackaged()) {
            // data值整体加密
            JsonNode dataNode = rootNode.get("data");
            if (dataNode != null) {
                String data = dataNode.toString();
                if (StringUtils.isNotBlank(data)) {
                    ((ObjectNode) rootNode).put("data", envelopeCryptoService.encrypt2String(data, caller.getPublicKey(), ""));
                }
            }
        } else {
            // data数据中的关键字段值加密
            Set<String> keyFields = defenceProperties.getKeyFields();
            encryptFields("", rootNode, keyFields, caller.getPublicKey());
        }
        String encryptedData = JsonUtils.getMapper().writeValueAsString(rootNode);
        // 将加密后的数据写入原始响应流
        wrapper.write(encryptedData);
    }

    /**
     * 从包装体中逐层抽取
     *
     * @param response
     * @return
     */
    private CachedHttpServletResponseWrapper loadCachedHttpServletResponseWrapper(ServletResponse response) {
        if (response instanceof CachedHttpServletResponseWrapper) {
            return (CachedHttpServletResponseWrapper) response;
        } else if (response instanceof HttpServletResponseWrapper) {
            ServletResponse servletResponse = ((HttpServletResponseWrapper) response).getResponse();
            return loadCachedHttpServletResponseWrapper(servletResponse);
        } else {
            return null;
        }
    }

    private boolean isFormRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (StringUtils.isBlank(contentType)) {
            return false;
        }
        if (contentType.startsWith("application/x-www-form-urlencoded") || contentType.startsWith("multipart/form-data")) {
            return true;
        }
        return false;
    }

    private String buildDecryptedUri(String uriTemplate, Map<String, String> decryptedVariables) {
        String decryptedUri = uriTemplate;
        for (Map.Entry<String, String> entry : decryptedVariables.entrySet()) {
            String placeholder = CommonConstants.DELIM_START + entry.getKey() + CommonConstants.DELIM_END;
            decryptedUri = decryptedUri.replace(placeholder, entry.getValue());
        }
        return decryptedUri;
    }

    private void decryptFields(String nodeKey, JsonNode node, Set<String> keyFields, String privateKey) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode valueNode = entry.getValue();
                if (!valueNode.isObject() && !valueNode.isArray() && keyFields.contains(key)) {
                    // 最底层叶子节点才需要解密
                    objectNode.put(key, envelopeCryptoService.decrypt2String(valueNode.asText(), privateKey, ""));
                } else {
                    decryptFields(key, valueNode, keyFields, privateKey);
                }
            });
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode element = arrayNode.get(i);
                if (!element.isObject() && !element.isArray() && keyFields.contains(nodeKey)) {
                    // 最底层叶子节点才需要解密
                    arrayNode.set(i, arrayNode.textNode(envelopeCryptoService.decrypt2String(element.asText(), privateKey, "")));
                } else {
                    decryptFields(nodeKey, element, keyFields, privateKey);
                }
            }
        }
    }

    private void encryptFields(String nodeKey, JsonNode node, Set<String> keyFields, String publicKey) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode valueNode = entry.getValue();
                if (!valueNode.isObject() && !valueNode.isArray() && keyFields.contains(key)) {
                    // 最底层叶子节点才需要加密
                    objectNode.put(key, envelopeCryptoService.encrypt2String(valueNode.asText(), publicKey, ""));
                } else {
                    encryptFields(key, valueNode, keyFields, publicKey);
                }
            });
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode element = arrayNode.get(i);
                if (!element.isObject() && !element.isArray() && keyFields.contains(nodeKey)) {
                    // 最底层叶子节点才需要加密
                    arrayNode.set(i, arrayNode.textNode(envelopeCryptoService.encrypt2String(element.asText(), publicKey, "")));
                } else {
                    encryptFields(nodeKey, element, keyFields, publicKey);
                }
            }
        }
    }

}
