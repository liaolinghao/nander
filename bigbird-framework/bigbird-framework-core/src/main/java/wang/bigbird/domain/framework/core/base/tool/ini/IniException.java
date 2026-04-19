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
package wang.bigbird.domain.framework.core.base.tool.ini;

/**
 * INI解析相关异常
 *
 * @author Bigbird
 */
public class IniException extends RuntimeException {

    private static final long serialVersionUID = -6096483218691358776L;

    /**
     * 包装异常消息
     *
     * @param e 异常消息
     */
    public IniException(Throwable e) {
        super(e);
    }

    /**
     * 包装异常消息
     *
     * @param message 异常信息
     */
    public IniException(String message) {
        super(message);
    }

    /**
     * 包装异常消息
     *
     * @param message 异常信息
     * @param cause   异常消息
     */
    public IniException(String message, Throwable cause) {
        super(message, cause);
    }

}
