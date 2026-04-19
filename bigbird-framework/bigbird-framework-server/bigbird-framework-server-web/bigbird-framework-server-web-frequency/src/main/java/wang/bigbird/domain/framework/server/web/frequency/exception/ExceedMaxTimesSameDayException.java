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
package wang.bigbird.domain.framework.server.web.frequency.exception;

import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.server.core.support.response.IBaseResponseStatus;

/**
 * 当天内超过最大次数
 *
 * @author Bigbird
 */
public class ExceedMaxTimesSameDayException extends FrequencyRuntimeException {

    private static final long serialVersionUID = -5096723689205148402L;

    public ExceedMaxTimesSameDayException() {
        super(IBaseResponseStatus.EXCEED_MAX_TIMES_SAME_DAY);
    }

    public ExceedMaxTimesSameDayException(String message) {
        super(IBaseResponseStatus.EXCEED_MAX_TIMES_SAME_DAY.getCode(), StringUtils.isBlank(message) ? IBaseResponseStatus.EXCEED_MAX_TIMES_SAME_DAY.getMessage() : message);
    }

}
