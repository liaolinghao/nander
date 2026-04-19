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
package wang.bigbird.domain.framework.server.web.defence.exception;

/**
 * 重放异常
 *
 * @author Bigbird
 */
public class ReplayAttackException extends DefenceException {

    private static final long serialVersionUID = -614380352041035843L;

    public ReplayAttackException() {
        super();
    }

    public ReplayAttackException(String message) {
        super(message);
    }

    public ReplayAttackException(String message, Throwable cause) {
        super(message, cause);
    }

}
