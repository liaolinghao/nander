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
package wang.bigbird.domain.framework.server.web.dao.support.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import wang.bigbird.domain.framework.server.core.support.response.IBaseResponseStatus;
import wang.bigbird.domain.framework.server.core.support.response.RespResult;

/**
 * 异常统一处理
 *
 * @author Bigbird
 * @ControllerAdvice给Controller控制器添加统一的操作或处理
 * @ExceptionHandler捕获异常 以上两个注解组合完成统一异常处理，当存在多个@RestControllerAdvice时，
 * 配置@Order(1)用于设置自身服务的拦截顺序为第二优先级
 */
@Slf4j
@RestControllerAdvice
@Order(1)
public class DaoExceptionHandler {

    /**
     * 处理DuplicateKeyException
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(DuplicateKeyException.class)
    public RespResult<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn(e.getMessage(), e);
        return RespResult.of(IBaseResponseStatus.DUPLICATE_BUSINESS_DATA);
    }

}
