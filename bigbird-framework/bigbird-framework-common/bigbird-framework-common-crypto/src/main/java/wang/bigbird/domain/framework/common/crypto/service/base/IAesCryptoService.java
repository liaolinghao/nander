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
package wang.bigbird.domain.framework.common.crypto.service.base;

import wang.bigbird.domain.framework.common.crypto.base.enums.AesKeyLengthEnum;

/**
 * AES加密解密服务，对称算法，加密和解密都是用同一个密钥
 *
 * @author Bigbird
 */
public interface IAesCryptoService extends ICryptoService {

    /**
     * 获取密钥对应的字节数组
     *
     * @param key 密钥
     * @return 密钥对应的字节数组
     */
    byte[] getKey(String key);

    /**
     * 生成密钥
     *
     * @param seed      生成密钥的种子，可以理解为密钥的唯一标记
     * @param keyLength 生成的密钥长度
     * @return 返回base64编码字符串
     */
    String createKey(String seed, AesKeyLengthEnum keyLength);
}
