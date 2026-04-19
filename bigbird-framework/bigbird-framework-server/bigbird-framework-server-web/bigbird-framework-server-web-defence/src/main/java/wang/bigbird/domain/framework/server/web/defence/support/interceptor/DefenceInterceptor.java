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
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import wang.bigbird.domain.framework.server.web.defence.support.security.AccessData;
import wang.bigbird.domain.framework.server.web.defence.support.security.DefenceGateway;
import wang.bigbird.domain.framework.server.web.defence.support.annotation.Defenceless;
import wang.bigbird.domain.framework.server.web.defence.domain.pojo.CallerItem;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 安全防护访问拦截器
 *
 * @author Bigbird
 */
@Slf4j
@Component
public class DefenceInterceptor extends AbstractInterceptor {

    /**
     * 在请求匹配controller之前执行，返回true才行进行下一步
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        Defenceless defenceless = method.getAnnotation(Defenceless.class);
        if (null != defenceless) {
            String methodName = method.getName();
            log.info("The method {} is defenceless.", methodName);
            return true;
        }
        AccessData accessData = loadAccessData(request,handler);
        CallerItem caller = getCaller(accessData);
        if (caller.getTrusted()) {
            // 可信调用者，不进行调用防御验证
            return true;
        }
        DefenceGateway.accessControl(caller,accessData);
        return true;
    }

    /**
     * 已经执行完controller了，但是还没有进入视图渲染
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

    /**
     * 视图也渲染完了，此时可以做一些清理工作了
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

}
