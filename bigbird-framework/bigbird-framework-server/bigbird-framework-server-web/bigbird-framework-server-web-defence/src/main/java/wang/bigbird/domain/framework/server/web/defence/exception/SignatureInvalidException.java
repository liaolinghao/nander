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
 * 签名检查异常
 *
 * @author Bigbird
 */
public class SignatureInvalidException extends DefenceException {

    private static final long serialVersionUID = 8613596346384732456L;

    public SignatureInvalidException() {
        super();
    }

    public SignatureInvalidException(String message) {
        super(message);
    }

    public SignatureInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

}
