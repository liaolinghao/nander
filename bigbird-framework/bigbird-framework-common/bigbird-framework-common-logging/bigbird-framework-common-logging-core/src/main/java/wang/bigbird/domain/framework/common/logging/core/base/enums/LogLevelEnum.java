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
package wang.bigbird.domain.framework.common.logging.core.base.enums;

/**
 * 日志级别定义
 *
 * @author Bigbird
 */
public enum LogLevelEnum {

    /**
     * trace日志级别
     */
    TRACE,
    /**
     * debug日志级别
     */
    DEBUG,
    /**
     * info日志级别
     */
    INFO,
    /**
     * warn日志级别
     */
    WARN,
    /**
     * error日志级别
     */
    ERROR;

    /**
     * 返回编码
     *
     * @return
     */
    public String getCode() {
        return this.name().toLowerCase();
    }

}
