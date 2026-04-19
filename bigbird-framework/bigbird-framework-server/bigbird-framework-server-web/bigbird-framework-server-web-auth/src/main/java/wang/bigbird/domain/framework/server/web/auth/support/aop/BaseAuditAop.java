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
package wang.bigbird.domain.framework.server.web.auth.support.aop;

import cn.hutool.core.collection.CollectionUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.server.web.auth.domain.bo.AuditLogBO;
import wang.bigbird.domain.framework.server.web.auth.support.annotation.AuditLog;
import wang.bigbird.domain.framework.server.web.core.base.util.HttpUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 审计日志处理
 *
 * @author Bigbird
 */
public abstract class BaseAuditAop {

    /**
     * pointcut 指定切入点的生效时机
     *
     * @annotation 当执行方法时拥有指定注解生效
     * @target 当代理目标拥有指定注解时生效
     */
    @Pointcut("@annotation(wang.bigbird.domain.framework.server.web.auth.support.annotation.AuditLog)" +
            "&& (@target(org.springframework.web.bind.annotation.RestController) " +
            "|| @target(org.springframework.stereotype.Controller))")
    public void pointcut() {
        // do nothing
    }

    /**
     * 处理审计日志
     *
     * @param joinPoint 注入点对象
     * @return 注入点执行对象
     * @throws Throwable
     */
    @Around(value = "pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed = joinPoint.proceed();
        Long authId = parseAuthId();
        if (authId != null) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Object[] args = joinPoint.getArgs();
            // 获取方法 参数名:参数值,参数名:参数值,... 字符串
            String paramAndValue = methodParamHandle(method, args);
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String ip = HttpUtils.getRemoteAddr(request);
            AuditLog auditLog = method.getAnnotation(AuditLog.class);
            AuditLogBO auditLogBO = new AuditLogBO();
            auditLogBO.setPlatform(auditLog.platform());
            auditLogBO.setModule(auditLog.module());
            auditLogBO.setDescription(auditLog.description());
            auditLogBO.setMode(auditLog.mode());
            auditLogBO.setAuthId(authId);
            auditLogBO.setIp(ip);
            auditLogBO.setParamAndValue(paramAndValue);
            processAuditLog(auditLogBO);
        }
        return proceed;
    }

    private String methodParamHandle(Method method, Object[] args) {
        String[] parameterNames = new LocalVariableTableParameterNameDiscoverer().getParameterNames(method);
        List<String> paramAndValues = new ArrayList<>(args.length);
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (!canSerialize(arg)) {
                continue;
            }
            String serialize = serialize(arg);
            if (null == serialize) {
                serialize = CommonConstants.EMPTY;
            }
            String paramAndValue;
            if (null != parameterNames) {
                String parameterName = parameterNames[i];
                paramAndValue = parameterName + CommonConstants.COLON + serialize;
            } else {
                paramAndValue = serialize;
            }
            paramAndValues.add(paramAndValue);
        }
        if (CollectionUtil.isEmpty(paramAndValues)) {
            return CommonConstants.EMPTY;
        } else {
            return String.join(CommonConstants.COMMA, paramAndValues);
        }
    }

    /**
     * 序列化
     */
    private String serialize(Object o) {
        if (null == o) {
            return null;
        }
        try {
            return JsonUtils.getMapper().writeValueAsString(o);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 能否进行序列化
     *
     * @return 能否进行序列化
     */
    private boolean canSerialize(Object o) {
        return !(o instanceof HttpServletRequest)
                && !(o instanceof HttpServletResponse)
                && !(o instanceof MultipartFile)
                && !(o instanceof MultipartFile[])
                && !(o instanceof FilePart)
                && !(o instanceof FilePart[])
                && !(o instanceof byte[]);
    }

    /**
     * 从请求中提取当前认证对象ID
     *
     * @return 认证对象ID
     */
    private Long parseAuthId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        String credentials = (String) authentication.getCredentials();
        return Long.valueOf(credentials.split(CommonConstants.SEPARATOR)[1]);
    }

    /**
     * 处理审计日志
     *
     * @param auditLogBO 审计日志信息
     */
    protected abstract void processAuditLog(AuditLogBO auditLogBO);

}
