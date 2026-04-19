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
package wang.bigbird.domain.framework.common.crypto.base.enums;

/**
 * AES 密钥长度类型
 *
 * 分为128位长度，192位长度，256位长度三种，
 * 密钥越长，安全强度越高，加密解密时间越久
 *
 * @author Bigbird
 */
public enum AesKeyLengthEnum {

    /**
     * 128位长度
     */
    BIT_128(128),
    /**
     * 192位长度
     */
    BIT_192(192),
    /**
     * 256位长度
     */
    BIT_256(256);

    public final Integer value;

    AesKeyLengthEnum(Integer value) {
        this.value = value;
    }
}
