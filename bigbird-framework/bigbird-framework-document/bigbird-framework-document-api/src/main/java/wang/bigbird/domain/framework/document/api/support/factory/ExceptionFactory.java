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
package wang.bigbird.domain.framework.document.api.support.factory;

import wang.bigbird.domain.framework.document.api.exception.DocFormatRuleParseException;

/**
 * 异常工厂，用于封装文档制作规则解析相关异常
 *
 * @author Bigbird
 */
public class ExceptionFactory {

    /**
     * 禁止实例化
     */
    private ExceptionFactory() {

    }

    /**
     * 创建无效的文档表格制作规则解析异常
     *
     * @return 文档制作规则解析相关异常
     */
    public static DocFormatRuleParseException buildInvalidTableFormatRuleException() {
        return new DocFormatRuleParseException("The formatRule for table is invalid!");
    }

    /**
     * 创建无效的文档图片制作规则解析异常
     *
     * @return 文档制作规则解析相关异常
     */
    public static DocFormatRuleParseException buildInvalidImageFormatRuleException() {
        return new DocFormatRuleParseException("The formatRule for image is invalid!");
    }


}
