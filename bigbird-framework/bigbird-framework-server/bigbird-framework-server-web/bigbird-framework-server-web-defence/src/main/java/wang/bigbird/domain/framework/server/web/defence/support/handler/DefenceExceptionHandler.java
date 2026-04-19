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
package wang.bigbird.domain.framework.server.web.defence.support.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import wang.bigbird.domain.framework.server.core.support.response.IBaseResponseStatus;
import wang.bigbird.domain.framework.server.core.support.response.RespResult;
import wang.bigbird.domain.framework.server.web.defence.exception.*;

import javax.management.ServiceNotFoundException;

/**
 * 异常统一处理
 *
 * @author Bigbird
 * @ControllerAdvice给Controller控制器添加统一的操作或处理
 * @ExceptionHandler捕获异常 以上两个注解组合完成统一异常处理，当存在多个@RestControllerAdvice时，
 * 配置@Order(Ordered.HIGHEST_PRECEDENCE)用于设置自身服务的拦截顺序为最高优先级
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DefenceExceptionHandler {

    /**
     * 处理BadRequestDataException
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestDataException.class)
    public RespResult<Void> handleBadRequestDataException(BadRequestDataException e) {
        log.warn(e.getMessage(), e);
        return RespResult.of(IBaseResponseStatus.BAD_REQUEST_DATA);
    }

    /**
     * 处理CallerNotFoundException
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CallerNotFoundException.class)
    public RespResult<Void> handleCallerNotFoundException(CallerNotFoundException e) {
        log.warn(e.getMessage(), e);
        return RespResult.of(IBaseResponseStatus.CALLER_NOT_FOUND);
    }

    /**
     * 处理IpInvalidException
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IpInvalidException.class)
    public RespResult<Void> handleIpInvalidException(IpInvalidException e) {
        log.warn(e.getMessage(), e);
        return RespResult.of(IBaseResponseStatus.IP_INVALID);
    }

    /**
     * 处理LimitListValidateException
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(LimitListValidateException.class)
    public RespResult<Void> handleLimitListValidateException(LimitListValidateException e) {
        log.warn(e.getMessage(), e);
        return RespResult.of(IBaseResponseStatus.LIMIT_LIST_VALIDATE);
    }

    /**
     * 处理ReplayAttackException
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ReplayAttackException.class)
    public RespResult<Void> handleReplayAttackException(ReplayAttackException e) {
        log.warn(e.getMessage(), e);
        return RespResult.of(IBaseResponseStatus.REPLAY_ATTACK);
    }

    /**
     * 处理ServiceNotFoundException
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ServiceNotFoundException.class)
    public RespResult<Void> handleBadRequestDataException(ServiceNotFoundException e) {
        log.warn(e.getMessage(), e);
        return RespResult.of(IBaseResponseStatus.SERVICE_NOT_FOUND);
    }

    /**
     * 处理SignatureInvalidException
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SignatureInvalidException.class)
    public RespResult<Void> handleSignatureInvalidException(SignatureInvalidException e) {
        log.warn(e.getMessage(), e);
        return RespResult.of(IBaseResponseStatus.SIGNATURE_INVALID);
    }

}
