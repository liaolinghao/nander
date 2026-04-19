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
package wang.bigbird.domain.framework.server.web.core.base.enums;

/**
 * 加解密类型
 *
 * @author Bigbird
 */
public enum CryptoEnum {

    /**
     * 简单加密解密
     */
    SIMPLE,
    /**
     * AES加密解密
     */
    AES,
    /**
     * RSA加密解密
     */
    RSA,
    /**
     * SM4加密解密
     */
    SM4,
    /**
     * 数字信封加密解密
     */
    ENVELOPE;

}
