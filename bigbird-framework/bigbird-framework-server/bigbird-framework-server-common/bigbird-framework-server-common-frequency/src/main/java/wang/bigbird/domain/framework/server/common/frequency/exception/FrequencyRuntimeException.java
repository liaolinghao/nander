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
package wang.bigbird.domain.framework.server.common.frequency.exception;

import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.server.core.exception.BaseBusinessException;
import wang.bigbird.domain.framework.server.core.support.response.IBaseResponseStatus;
import wang.bigbird.domain.framework.server.core.support.response.ResponseStatus;


/**
 * 频率超过阀值
 *
 * @author Bigbird
 */
public class FrequencyRuntimeException extends BaseBusinessException {

    private static final long serialVersionUID = 3766551854095939738L;

    public FrequencyRuntimeException() {
        super(IBaseResponseStatus.EXCEED_MAX_TIMES);
    }

    public FrequencyRuntimeException(String message) {
        super(IBaseResponseStatus.EXCEED_MAX_TIMES.getCode(), StringUtils.isBlank(message) ? IBaseResponseStatus.EXCEED_MAX_TIMES.getMessage() : message);
    }

    public FrequencyRuntimeException(ResponseStatus responseStatus) {
        super(responseStatus);
    }

    public FrequencyRuntimeException(Integer code, String msg) {
        super(code, msg);
    }

}
