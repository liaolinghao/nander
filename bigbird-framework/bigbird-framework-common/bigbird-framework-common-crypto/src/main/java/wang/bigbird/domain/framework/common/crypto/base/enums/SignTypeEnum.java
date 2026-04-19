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

import lombok.Getter;

/**
 * 签名算法采用的加密方式
 *
 * @author Bigbird
 */
@Getter
public enum SignTypeEnum {
    /**
     * MD5加密，加密长度为32位
     */
    MD5("MD5"),
    /**
     * sha1加密，加密长度为40位
     */
    SHA1("SHA"),
    /**
     * sha256加密，加密长度为64位
     */
    SHA_256("SHA-256"),
    /**
     * sha512加密，加密长度为128位
     */
    SHA_512("SHA-512"),
    /**
     * SM3加密，加密长度为64位
     */
    SM3("SM3");

    private final String code;

    SignTypeEnum(String code) {
        this.code = code;
    }
}
