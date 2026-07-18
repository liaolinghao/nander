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
package wang.bigbird.domain.framework.server.web.auth.support.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.server.core.support.holder.SpringContextHolder;
import wang.bigbird.domain.framework.server.core.support.response.IBaseResponseStatus;
import wang.bigbird.domain.framework.server.core.support.response.RespResult;
import wang.bigbird.domain.framework.server.web.auth.config.property.JwtSecurityProperties;
import wang.bigbird.domain.framework.server.web.auth.exception.DisposedJwtException;
import wang.bigbird.domain.framework.server.web.auth.exception.KickOffJwtException;
import wang.bigbird.domain.framework.server.web.auth.support.processor.JwtSecurityProcessor;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 执行jwt token验证的过滤器
 *
 * @author Bigbird
 */
@Component
@Slf4j
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private static final String APPKEY_PARAM_CODE = "appKey";
    private static final String APPKEY_HEADER_CODE = "AppKey";

    private static final AntPathMatcher matcher = new AntPathMatcher();

    private JwtSecurityProcessor jwtSecurityProcessor;

    public JwtAuthenticationTokenFilter(JwtSecurityProcessor jwtSecurityProcessor) {
        this.jwtSecurityProcessor = jwtSecurityProcessor;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        JwtSecurityProperties jwtSecurityProperties = SpringContextHolder.getBean(JwtSecurityProperties.class);
        String requestUri = httpServletRequest.getRequestURI();
        String contextPath = httpServletRequest.getContextPath();
        if (isWithoutJwtSecurityApi(requestUri, contextPath)) {
            // 如果是免认证接口，直接放行
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
        //获取request token
        String token = null;
        String bearerToken = httpServletRequest.getHeader(jwtSecurityProperties.getHeader());
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(jwtSecurityProperties.getTokenStartWith())) {
            token = bearerToken.substring(jwtSecurityProperties.getTokenStartWith().length());
        }
        if (StringUtils.hasText(token)) {
            try {
                String appKey = httpServletRequest.getParameter(APPKEY_PARAM_CODE);
                if (StringUtils.isBlank(appKey)) {
                    appKey = httpServletRequest.getHeader(APPKEY_HEADER_CODE);
                }
                Authentication authentication = jwtSecurityProcessor.getAuthentication(token, appKey);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Set Authentication to security context for '{}', uri: {}.", authentication.getName(), requestUri);
            } catch (Exception e) {
                // JWT不合格，http状态码统一修正为400
                log.error("Invalid token:{},{}", token, e.getMessage(), e);
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.setContentType("application/json;charset=utf-8");
                if (e instanceof io.jsonwebtoken.security.SecurityException || e instanceof MalformedJwtException) {
                    httpServletResponse.getWriter().println(JsonUtils.object2Json(RespResult.of(IBaseResponseStatus.INVALID_JWT_SIGNATURE)));
                } else if (e instanceof ExpiredJwtException) {
                    httpServletResponse.getWriter().println(JsonUtils.object2Json(RespResult.of(IBaseResponseStatus.EXPIRED_JWT)));
                } else if (e instanceof UnsupportedJwtException) {
                    httpServletResponse.getWriter().println(JsonUtils.object2Json(RespResult.of(IBaseResponseStatus.UNSUPPORTED_JWT)));
                } else if (e instanceof KickOffJwtException) {
                    httpServletResponse.getWriter().println(JsonUtils.object2Json(RespResult.of(IBaseResponseStatus.KICK_OFF_JWT)));
                } else if (e instanceof DisposedJwtException) {
                    httpServletResponse.getWriter().println(JsonUtils.object2Json(RespResult.of(IBaseResponseStatus.DISPOSED_JWT)));
                } else {
                    httpServletResponse.getWriter().println(JsonUtils.object2Json(RespResult.of(IBaseResponseStatus.INVALID_JWT)));
                }
                return;
            }
        } else {
            log.debug("No valid JWT token found, uri: {}.", requestUri);
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private boolean isWithoutJwtSecurityApi(String requestUri, String contextPath) {
        String[] apis = jwtSecurityProcessor.getWithoutJwtSecurityApi();
        for (String api : apis) {
            if (matcher.match(StringUtils.joinStr(contextPath, api), requestUri)) {
                return true;
            }
        }
        return false;
    }

}
