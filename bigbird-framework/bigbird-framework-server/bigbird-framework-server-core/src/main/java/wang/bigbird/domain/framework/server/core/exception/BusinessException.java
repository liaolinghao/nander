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
package wang.bigbird.domain.framework.server.core.exception;


import wang.bigbird.domain.framework.server.core.support.response.ResponseStatus;

/**
 * WEB 层的异常派生类
 *
 * @author Bigbird
 */
public class BusinessException extends BaseBusinessException {

    private static final long serialVersionUID = -6767524800355311760L;

    public BusinessException() {
        super();
    }

    public BusinessException(Integer code, String msg) {
        super(code, msg);
    }

    public BusinessException(ResponseStatus responseStatus) {
        super(responseStatus);
    }

    public BusinessException(Integer code, String msg, Object data) {
        super(code, msg, data);
    }

    public BusinessException(Integer code, String msg, Throwable throwable) {
        super(code, msg, throwable);
    }

    public BusinessException(Integer code, String msg, Object data, Throwable throwable) {
        super(code, msg, data, throwable);
    }

    public BusinessException(Integer code, Object data, String messageTemplate, Object... params) {
        super(code, data, messageTemplate, params);
    }

    public BusinessException(Throwable throwable, Integer code, Object data, String messageTemplate, Object... params) {
        super(throwable, code, data, messageTemplate, params);
    }

    public static BusinessException of(Integer code, String msg) {
        return new BusinessException(code, msg);
    }

    public static BusinessException of(Integer code, String msg, Object data) {
        return new BusinessException(code, msg, data);
    }

    public static BusinessException of(Integer code, String msg, Throwable throwable) {
        return new BusinessException(code, msg, throwable);
    }

    public static BusinessException of(Integer code, String msg, Object data, Throwable throwable) {
        return new BusinessException(code, msg, data, throwable);
    }

    public static BusinessException of(ResponseStatus responseStatus) {
        return new BusinessException(responseStatus.getCode(), responseStatus.getMessage());
    }

    public static BusinessException of(ResponseStatus responseStatus, Object data) {
        return new BusinessException(responseStatus.getCode(), responseStatus.getMessage(), data);
    }

    public static BusinessException of(ResponseStatus responseStatus, Throwable throwable) {
        return new BusinessException(responseStatus.getCode(), responseStatus.getMessage(), throwable);
    }

    public static BusinessException of(ResponseStatus responseStatus, Object data, Throwable throwable) {
        return new BusinessException(responseStatus.getCode(), responseStatus.getMessage(), data, throwable);
    }

    /**
     * 支持msg中有{}占位符的异常信息
     *
     * @param throwable      异常
     * @param responseStatus 异常码
     * @param params         占位符参数
     */
    public static BusinessException of(Throwable throwable, ResponseStatus responseStatus, Object... params) {
        if (null != throwable) {
            return new BusinessException(throwable, responseStatus.getCode(), null, responseStatus.getMessage(), params);
        }
        return new BusinessException(responseStatus.getCode(), null, responseStatus.getMessage(), params);
    }

}
