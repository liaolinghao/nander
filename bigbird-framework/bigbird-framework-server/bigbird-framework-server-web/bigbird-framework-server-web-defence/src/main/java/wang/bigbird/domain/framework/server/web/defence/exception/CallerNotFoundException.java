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
 * 接入系统未注册异常
 *
 * 接入系统需要在tb_app表中注册
 *
 * @author Bigbird
 */
public class CallerNotFoundException extends DefenceException {

    private static final long serialVersionUID = 3662366128503275618L;

    public CallerNotFoundException() {
        super();
    }

    public CallerNotFoundException(String message) {
        super(message);
    }

    public CallerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
