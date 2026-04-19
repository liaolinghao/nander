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
package wang.bigbird.domain.framework.common.sensitive.base.constant;

/**
 * 脱敏处理相关的常量定义
 *
 * @author Bigbird
 */
public class SensitiveConstant {

    private SensitiveConstant() {
        throw new IllegalStateException();
    }

    public static final int FRONT_AND_BACK_FRONT_LEN = 3;

    public static final int FRONT_AND_BACK_BACK_LEN = 3;

    public static final String DESENSITIZATION_STR = "*";

    public static final String DEFAULT_SYMBOL = "******";

}
