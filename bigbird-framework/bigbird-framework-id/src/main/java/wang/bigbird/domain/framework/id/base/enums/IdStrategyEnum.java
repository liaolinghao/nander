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
package wang.bigbird.domain.framework.id.base.enums;

import wang.bigbird.domain.framework.core.base.util.enums.ValuedEnum;

/**
 * ID生成策略
 *
 * @author Bigbird
 */
public enum IdStrategyEnum implements ValuedEnum<String> {

    /**
     * twitter 雪花ID模式
     */
    snowflake("snowflake"),

    /**
     * 美团leaf segment模式
     */
    segment("segment"),

    /**
     * 百度uid模式
     */
    uid("uid");

    private final String name;

    IdStrategyEnum(String name) {
        this.name = name;
    }

    @Override
    public String value() {
        return name;
    }

}
