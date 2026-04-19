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

/**
 * 加密解密服务
 *
 * @author Bigbird
 */
public interface ICryptoService {

    /**
     * 加密
     *
     * @param data 需要加密数据。
     * @param key  加密用的key，一般非对称加密是publicKey
     * @param iv   加密初始向量
     * @return 加密后的数组
     */
    byte[] encrypt(byte[] data, String key, String iv);

    /**
     * 加密
     *
     * @param text 需要加密数据。
     * @param key  加密用的key，一般非对称加密是publicKey
     * @param iv   加密初始向量
     * @return 加密后的字符串，该字符串由对应字节转换器将加密生成的字节数组构造获得。
     */
    String encrypt2String(String text, String key, String iv);

    /**
     * 解密
     *
     * @param data 需要解密数据。
     * @param key  解密用的key，一般非对称解密是privateKey
     * @param iv   加密初始向量
     * @return 解密后的数据
     */
    byte[] decrypt(byte[] data, String key, String iv);

    /**
     * 解密
     *
     * @param text 需要解密的字符串数据，
     *             该字符串需要由对应字节转换器解码成对应的加密字节数组后再执行解密。
     * @param key  解密用的key，一般非对称解密是privateKey
     * @param iv   加密初始向量
     * @return 解密后的原始字符串
     */
    String decrypt2String(String text, String key, String iv);
}
