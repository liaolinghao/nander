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
package wang.bigbird.domain.framework.server.web.captcha.exception;


import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.server.core.exception.BaseBusinessException;
import wang.bigbird.domain.framework.server.core.support.response.IBaseResponseStatus;


/**
 * 背景图路径里面是空
 *
 * @author Bigbird
 */
public class BackGroundImageIsEmptyException extends BaseBusinessException {

    public BackGroundImageIsEmptyException() {
        super(IBaseResponseStatus.DATA_NOT_EXIST);
    }

    public BackGroundImageIsEmptyException(String message) {
        super(IBaseResponseStatus.DATA_NOT_EXIST.getCode(), StringUtils.isBlank(message) ? IBaseResponseStatus.DATA_NOT_EXIST.getMessage() : message);
    }

}
