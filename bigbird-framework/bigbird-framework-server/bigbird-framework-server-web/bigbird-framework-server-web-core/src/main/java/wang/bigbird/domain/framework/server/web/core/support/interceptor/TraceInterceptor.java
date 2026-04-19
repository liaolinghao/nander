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

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import wang.bigbird.domain.framework.common.logging.core.base.constant.LogConstants;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Rest请求链路追踪拦截器，用于为每个rest请求添加traceId，以方便定位请求日志
 *
 * @author Bigbird
 */
@Component
public class TraceInterceptor extends HandlerInterceptorAdapter {

    private static final String HEADER_TRACE_ID = "TraceId";
    private static final String PARAMETER_TRACE_ID = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String traceId = getTraceId(request);
        MDC.put(LogConstants.TRACE_ID, traceId);
        //将traceId添加进响应头
        response.addHeader(HEADER_TRACE_ID, traceId);
        return true;
    }

    private String getTraceId(HttpServletRequest request) {
        String traceId = request.getHeader(HEADER_TRACE_ID);
        if (StringUtils.isBlank(traceId)) {
            traceId = request.getParameter(PARAMETER_TRACE_ID);
        }
        if (StringUtils.isBlank(traceId)) {
            traceId = StringUtils.getUuid();
        }
        return String.format("%s-%s-%s", request.getRequestURI(), request.getMethod(), traceId);
    }

}
