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

import cn.hutool.core.util.StrUtil;
import wang.bigbird.domain.framework.server.core.support.response.ResponseStatus;

/**
 * 业务异常基类，作为各类自定义业务异常的基类
 * 默认继承了 BaseBusinessException 的派生异常类可以通过WEB框架统一异常处理捕获到。
 *
 * @author Bigbird
 */
public class BaseBusinessException extends RuntimeException {

    private static final long serialVersionUID = 5079143801268203254L;

    protected Integer code;

    protected Object data;

    public BaseBusinessException() {
        super();
    }

    public BaseBusinessException(String message) {
        super(message);
    }

    public BaseBusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseBusinessException(Throwable cause) {
        super(cause);
    }

    public BaseBusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }

    public BaseBusinessException(ResponseStatus respCodeEntity) {
        super(respCodeEntity.getMessage());
        this.code = respCodeEntity.getCode();
    }

    public BaseBusinessException(Integer code, String msg, Object data) {
        super(msg);
        this.code = code;
        this.data = data;
    }

    public BaseBusinessException(Integer code, String msg, Throwable throwable) {
        super(msg, throwable);
        this.code = code;
    }

    public BaseBusinessException(Integer code, String msg, Object data, Throwable throwable) {
        super(msg, throwable);
        this.code = code;
        this.data = data;
    }

    public BaseBusinessException(Integer code, Object data, String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params));
        this.code = code;
        this.data = data;
    }

    public BaseBusinessException(Throwable throwable, Integer code, Object data, String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params), throwable);
        this.code = code;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public Object getData() {
        return data;
    }
}
