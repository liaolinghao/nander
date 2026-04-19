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
package wang.bigbird.domain.framework.server.web.core.base.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.tool.Coder;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Http协议解析通用工具
 * 对于完整接口请求：http://localhost:8080/myapp/user/list?page=1&size=10
 * request.getRequestURI()：/myapp/user/list	上下文路径 + 资源路径
 * request.getContextPath()：/myapp	仅上下文路径
 * request.getServletPath()：/user/list（取决于 Servlet 映射）	仅 Servlet 映射的路径部分
 * request.getRequestURL()：http://localhost:8080/myapp/user/list	完整 URL（含协议、域名、端口、路径）
 * request.getQueryString()：page=1&size=10
 *
 * @author Bigbird
 */
@Slf4j
public class HttpUtils {

    private static final String UNKNOWN_IP = "unknown";

    /**
     * 获取客户端IP
     *
     * @param request
     * @return
     */
    public static String getRemoteAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isBlank(ip) || UNKNOWN_IP.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN_IP.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN_IP.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN_IP.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && (ip.indexOf(CommonConstants.COMMA) != -1)) {
            ip = ip.substring(0, ip.indexOf(CommonConstants.COMMA));
        }
        return ip;
    }

    /**
     * 获取接口地址模式串，包含contextPath
     *
     * @param handler
     * @param requestUri
     * @param contextPath
     * @return
     */
    public static String getRequestApi(Object handler, String requestUri, String contextPath) {
        HandlerMethod method = (HandlerMethod) handler;
        RequestMapping classMapping = method.getBeanType().getAnnotation(RequestMapping.class);
        RequestMapping methodMapping = method.getMethodAnnotation(RequestMapping.class);
        if (methodMapping == null || methodMapping.value() == null || methodMapping.value().length == 0) {
            return requestUri;
        }
        String[] methodMappingUri = methodMapping.value();
        String configUri = methodMappingUri[0];
        if (classMapping != null) {
            String[] classMappingUri = classMapping.value();
            configUri = classMappingUri[0] + methodMappingUri[0];
        }
        return contextPath + configUri;
    }

    /**
     * 获取接口请求头
     * 在HTTP协议中，请求头（Headers）的名称是大小写不敏感的。也就是说，无论请求头是以大写、小写还是大小写混合的形式发送，服务器都会将其视为相同的请求头。
     * 但是，在实践中，为了保证HTTP请求的清晰和一致性，大多数HTTP客户端（如浏览器、Postman等）和服务器在处理HTTP请求时，会将请求头自动转换为小写。
     *
     * @param request
     * @return
     */
    public static Map<String, String> getRequestHeader(HttpServletRequest request) {
        Enumeration headerNames = request.getHeaderNames();
        Map<String, String> strParams = new HashMap<>(CollectionUtils.initialMapCapacity(16));
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            // 此处必须转换为小写以兼容部分客户端将请求头自动转换为小写的问题
            strParams.put(key.toLowerCase(), value);
        }
        return strParams;
    }

    /**
     * 获取接口请求参数
     *
     * @param request
     * @return
     */
    public static Map<String, String> getRequestParam(HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        Map<String, String> strParams = new HashMap<>(CollectionUtils.initialMapCapacity(params.size()));
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String value = null;
            if (entry.getValue()[0] != null) {
                try {
                    value = Coder.urlDecoderUtf8(entry.getValue()[0]);
                } catch (Exception e) {
                    log.error("GetRequestParam:", e);
                    value = entry.getValue()[0];
                }
            }
            strParams.put(entry.getKey(), value);
        }
        return strParams;
    }

    /**
     * 获取请求内容体
     *
     * @param request
     * @return
     */
    public static String getRequestBody(HttpServletRequest request) throws IOException {
        ServletInputStream servletInputStream = request.getInputStream();
        if (servletInputStream.isReady()) {
            return IOUtils.toString(servletInputStream, Coder.DEFAULT_ENCODING);
        }
        return CommonConstants.EMPTY;
    }
}
