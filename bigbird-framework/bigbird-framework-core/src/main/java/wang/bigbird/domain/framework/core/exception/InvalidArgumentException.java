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
package wang.bigbird.domain.framework.core.exception;

/**
 * 无效参数异常
 * <p>
 * 用于特指符合目标格式，但是在业务上无效的参数传递场景
 *
 * @author Bigbird
 */
public class InvalidArgumentException extends RuntimeException {

    private static final long serialVersionUID = -5314747072777564932L;

    public InvalidArgumentException(String message) {
        super(message);
    }

}
