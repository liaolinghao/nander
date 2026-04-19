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
 * RSA密钥长度
 * <p>
 * RSA算法是一种非对称加密算法，这一算法主要依靠分解大素数的复杂性来实现其安全性，
 * RSA基于一个十分简单的数论事实：将两个大素数相乘十分容易，但想要对其乘积进行因式分解却极其困难，
 * 因此可以将乘积公开作为加密密钥，即公钥，而两个大素数组合成私钥。
 * 公钥是可发布的供任何人使用，私钥则为自己所有，供解密之用。
 *
 * @author Bigbird
 */
public enum RsaKeyBitsEnum {

    /**
     * 512位长度，64个字节
     */
    KEY_512(512, 64),
    /**
     * 1024位长度，128个字节
     */
    KEY_1024(1024, 128),
    /**
     * 2048位长度，256个字节
     */
    KEY_2048(2048, 256),
    /**
     * 4096位长度，512个字节
     */
    KEY_4096(4096, 512);

    /**
     * key 长度位数
     */
    public final Integer keyBits;
    /**
     * key 字节数
     */
    public final Integer keyBytes;

    RsaKeyBitsEnum(Integer keyBits, Integer keyBytes) {
        this.keyBits = keyBits;
        this.keyBytes = keyBytes;
    }
}
