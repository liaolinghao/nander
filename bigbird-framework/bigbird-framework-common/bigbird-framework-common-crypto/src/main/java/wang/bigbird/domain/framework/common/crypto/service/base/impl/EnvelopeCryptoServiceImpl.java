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
package wang.bigbird.domain.framework.common.crypto.service.base.impl;

import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.common.crypto.base.enums.AesKeyLengthEnum;
import wang.bigbird.domain.framework.common.crypto.base.util.BytesUtils;
import wang.bigbird.domain.framework.common.crypto.service.base.IAesCryptoService;
import wang.bigbird.domain.framework.common.crypto.service.base.IEnvelopeCryptoService;
import wang.bigbird.domain.framework.common.crypto.service.base.IRsaCryptoService;

import java.util.UUID;

/**
 * 数字信封加密解密工具
 * <p>
 * 数字信封是指发送方采用接收方的公钥来加密对称密钥后所得的数据。
 * 采用数字信封时，接收方需要使用自己的私钥才能打开数字信封得到对称密钥。
 * <p>
 * 甲事先获得乙的公钥，具体加解密过程如下：
 * 甲使用对称密钥对明文进行加密，生成密文信息。
 * 甲使用乙的公钥加密对称密钥，生成数字信封。
 * 甲将数字信封和密文信息一起发送给乙。
 * 乙接收到甲的加密信息后，使用自己的私钥打开数字信封，得到对称密钥。
 * 乙使用对称密钥对密文信息进行解密，得到最初的明文。
 * 从加解密过程中，可以看出，数字信封技术结合了对称密钥加密和公钥加密的优点，
 * 解决了对称密钥的发布和公钥加密速度慢等问题，提高了安全性、扩展性和效率等。
 * <p>
 * 缺点：数字信封技术还有个问题，如果攻击者拦截甲的信息，用自己的对称密钥加密伪造的信息，
 * 并用乙的公钥加密自己的对称密钥，然后发送给乙。乙收到加密信息后，解密得到的明文，
 * 而且乙始终认为是甲发送的信息。此时，需要一种方法确保接收方收到的信息就是指定的发送方发送的。
 *
 * @author Bigbird
 */
@Slf4j
@Setter
@Accessors(chain = true)
public class EnvelopeCryptoServiceImpl implements IEnvelopeCryptoService {
    /**
     * 非对称加密服务
     */
    private IRsaCryptoService rsaCryptoService;
    /**
     * 对称加密服务
     */
    private IAesCryptoService aesCryptoService;
    /**
     * 密钥长度
     */
    private AesKeyLengthEnum keyLength;

    public EnvelopeCryptoServiceImpl() {
        this.keyLength = AesKeyLengthEnum.BIT_128;
    }

    @Override
    public byte[] encrypt(byte[] data, @NonNull String publicKey, String iv) {
        BytesUtils.notEmpty(data);
        String key = aesCryptoService.createKey(UUID.randomUUID().toString(), keyLength);
        byte[] encryptData = aesCryptoService.encrypt(data, key, iv);
        key = rsaCryptoService.encrypt2String(key, publicKey, iv);
        String encrypt = key + " " + BytesUtils.toBase64(encryptData);
        return BytesUtils.fromBase64(encrypt);
    }

    @Override
    public String encrypt2String(@NonNull String text, @NonNull String publicKey, String iv) {
        String key = aesCryptoService.createKey(UUID.randomUUID().toString(), keyLength);
        String encryptData = aesCryptoService.encrypt2String(text, key, iv);
        key = rsaCryptoService.encrypt2String(key, publicKey, iv);
        return key + " " + encryptData;
    }

    @Override
    public byte[] decrypt(byte[] data, @NonNull String privateKey, String iv) {
        BytesUtils.notEmpty(data);
        String encrypt = BytesUtils.toBase64(data);
        String[] encryptData = encrypt.split(" ");
        String decryptKey = rsaCryptoService.decrypt2String(encryptData[0], privateKey, iv);
        return aesCryptoService.decrypt(BytesUtils.fromBase64(encryptData[1]), decryptKey, iv);
    }

    @Override
    public String decrypt2String(@NonNull String text, @NonNull String privateKey, String iv) {
        String[] data = text.split(" ");
        String decryptKey = rsaCryptoService.decrypt2String(data[0], privateKey, iv);
        return aesCryptoService.decrypt2String(data[1], decryptKey, iv);
    }
}
