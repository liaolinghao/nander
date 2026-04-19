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
package wang.bigbird.domain.framework.server.web.auth.support.handler;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import wang.bigbird.domain.framework.server.core.support.response.IBaseResponseStatus;
import wang.bigbird.domain.framework.server.core.support.response.RespResult;
import wang.bigbird.domain.framework.server.web.auth.exception.DisposedJwtException;
import wang.bigbird.domain.framework.server.web.auth.exception.KickOffJwtException;


/**
 * 异常统一处理
 *
 * @ControllerAdvice给Controller控制器添加统一的操作或处理
 * @ExceptionHandler捕获异常
 *
 * 以上两个注解组合完成统一异常处理，当存在多个@RestControllerAdvice时，
 * 配置@Order(0)用于设置自身服务的拦截顺序为中间优先级
 *
 * @author Bigbird
 */
@Slf4j
@RestControllerAdvice
@Order(0)
public class JwtExceptionHandler {

    /**
     * 处理SecurityException
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(io.jsonwebtoken.security.SecurityException.class)
    public RespResult<Void> handleAccessDeniedException(io.jsonwebtoken.security.SecurityException e) {
        log.warn(e.getMessage(), e);
        return RespResult.of(IBaseResponseStatus.INVALID_JWT_SIGNATURE);
    }

    /**
     * 处理MalformedJwtException
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MalformedJwtException.class)
    public RespResult<Void> handleMalformedJwtException(MalformedJwtException e) {
        log.warn(e.getMessage(), e);
        return RespResult.of(IBaseResponseStatus.INVALID_JWT_SIGNATURE);
    }

    /**
     * 处理ExpiredJwtException
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ExpiredJwtException.class)
    public RespResult<Void> handleExpiredJwtException(ExpiredJwtException e) {
        log.warn(e.getMessage(), e);
        return RespResult.of(IBaseResponseStatus.EXPIRED_JWT);
    }

    /**
     * 处理DisposedJwtException
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DisposedJwtException.class)
    public RespResult<Void> handleDisposedJwtException(DisposedJwtException e) {
        log.warn(e.getMessage(), e);
        return RespResult.of(IBaseResponseStatus.DISPOSED_JWT);
    }

    /**
     * 处理UnsupportedJwtException
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UnsupportedJwtException.class)
    public RespResult<Void> handleUnsupportedJwtException(UnsupportedJwtException e) {
        log.warn(e.getMessage(), e);
        return RespResult.of(IBaseResponseStatus.UNSUPPORTED_JWT);
    }

    /**
     * 处理KickOffJwtException
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(KickOffJwtException.class)
    public RespResult<Void> handleKickOffJwtException(KickOffJwtException e) {
        log.warn(e.getMessage(), e);
        return RespResult.of(IBaseResponseStatus.KICK_OFF_JWT);
    }

    /**
     * 处理AccessDeniedException
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public RespResult<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn(e.getMessage(), e);
        return RespResult.of(HttpStatus.FORBIDDEN.value(), e.getMessage());
    }

}
