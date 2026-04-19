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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.server.web.core.base.util.HttpUtils;
import wang.bigbird.domain.framework.server.web.defence.domain.pojo.CallerItem;
import wang.bigbird.domain.framework.server.web.defence.exception.BadRequestDataException;
import wang.bigbird.domain.framework.server.web.defence.exception.CallerNotFoundException;
import wang.bigbird.domain.framework.server.web.defence.exception.DefenceException;
import wang.bigbird.domain.framework.server.web.defence.service.cache.ICallerCacheService;
import wang.bigbird.domain.framework.server.web.defence.support.security.AccessData;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 抽象拦截器，定义各个拦截器要用到的公共方法
 *
 * @author Bigbird
 */
@Slf4j
public abstract class AbstractInterceptor implements HandlerInterceptor {

    @Autowired
    private ICallerCacheService callerCacheService;

    /**
     * 获取接口请求信息
     *
     * @param request
     * @param handler
     * @return
     * @throws IOException
     */
    protected AccessData loadAccessData(HttpServletRequest request, Object handler) throws IOException {
        AccessData accessData = new AccessData();
        accessData.setRemoteAddr(HttpUtils.getRemoteAddr(request));
        accessData.setRequestUri(request.getRequestURI());
        accessData.setRequestApi(HttpUtils.getRequestApi(handler, request.getRequestURI(), request.getContextPath()));
        accessData.setRequestAction(request.getMethod());
        accessData.setRequestBody(HttpUtils.getRequestBody(request));
        accessData.setRequestParam(HttpUtils.getRequestParam(request));
        accessData.setRequestHeader(HttpUtils.getRequestHeader(request));
        return accessData;
    }

    /**
     * 获取调用者信息
     *
     * @param accessData
     * @return 调用者信息
     * @throws DefenceException
     */
    protected CallerItem getCaller(AccessData accessData) throws DefenceException {
        if (accessData == null) {
            log.error("AccessData is null!");
            throw new BadRequestDataException("AccessData is null!");
        }
        String appKey = "";
        if (accessData.getRequestParam() != null) {
            appKey = accessData.getRequestParam().get(AccessData.APPKEY_PARAM_CODE);
        }
        if (StringUtils.isBlank(appKey) && accessData.getRequestHeader() != null) {
            appKey = accessData.getRequestHeader().get(AccessData.APPKEY_HEADER_CODE);
        }
        if (StringUtils.isBlank(appKey)) {
            log.error("appKey is null! AccessData: {}", accessData);
            throw new BadRequestDataException("appKey is null!");
        }
        CallerItem caller = callerCacheService.getCallerByAppKey(appKey);
        if (caller == null) {
            String errorInfo = String.format("Caller[%s] is not found!", appKey);
            log.error(errorInfo);
            throw new CallerNotFoundException(errorInfo);
        }
        return caller;
    }

}
