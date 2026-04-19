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
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import wang.bigbird.domain.framework.common.crypto.base.enums.Byte2StringTypeEnum;
import wang.bigbird.domain.framework.common.crypto.base.enums.Sm4KeyLengthEnum;
import wang.bigbird.domain.framework.common.crypto.base.tool.IBytesConverter;
import wang.bigbird.domain.framework.common.crypto.base.util.BytesUtils;
import wang.bigbird.domain.framework.common.crypto.exception.CryptoException;
import wang.bigbird.domain.framework.common.crypto.service.base.ISm4CryptoService;
import wang.bigbird.domain.framework.core.base.tool.Coder;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.security.Security;

/**
 * 国密SM4对称加密解密工具
 *
 * @author Bigbird
 */
@Slf4j
@Setter
@Accessors(chain = true)
public class Sm4CryptoServiceImpl implements ISm4CryptoService {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 初始向量 IV 的作用是使加密更加安全可靠，必须是16字节
     */
    private static final String INITIAL_VECTOR = "BIRD19841222ctdi";

    private static final String ALGORITHM_NAME = "SM4";

    /**
     * 加密算法/分组加密模式/分组填充方式
     * <p>
     * 填补 (Padding) 就是用来把不满16个字节的分组数据填满16个字节用的，它有三种模式 PKCS5、PKCS7 和 NOPADDING。
     * PKCS5 是指分组数据缺少几个字节，就在数据的末尾填充几个字节的几，比如缺少5个字节，就在末尾填充5个字节的5；
     * PKCS7 是指分组数据缺少几个字节，就在数据的末尾填充几个字节的0，比如缺少7个字节，就在末尾填充7个字节的0；
     * NoPadding 是指不需要填充，也就是说数据的发送方肯定会保证最后一段数据也正好是16个字节。
     * <p>
     * 特别提醒：使用 Nopadding 要加密数据必须是16字节的倍数，否则抛异常
     */
    private static final String ALGORITHM_NAME_CBC_PKCS_5_PADDING = "SM4/CBC/PKCS5Padding";
    /**
     * 字节转换器
     */
    private IBytesConverter bytesConverter = IBytesConverter.getInstance(Byte2StringTypeEnum.BASE64);

    @Override
    public String createKey(Sm4KeyLengthEnum keyLength) {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM_NAME, BouncyCastleProvider.PROVIDER_NAME);
            kg.init(keyLength.value, new SecureRandom());
            return BytesUtils.toHexString(kg.generateKey().getEncoded());
        } catch (Exception e) {
            throw new CryptoException("SM4 createKey error.", e);
        }
    }

    @Override
    public byte[] encrypt(byte[] data, @NonNull String publicKey, String iv) {
        BytesUtils.notEmpty(data);
        byte[] keyData = bytesConverter.decode(publicKey);
        return encrypt(keyData, data, iv);
    }

    @Override
    public String encrypt2String(@NonNull String text, @NonNull String publicKey, String iv) {
        return encrypt(publicKey, text, iv);
    }

    @Override
    public byte[] decrypt(byte[] data, @NonNull String privateKey, String iv) {
        BytesUtils.notEmpty(data);
        byte[] keyData = bytesConverter.decode(privateKey);
        return decrypt(keyData, data, iv);
    }

    @Override
    public String decrypt2String(@NonNull String text, @NonNull String privateKey, String iv) {
        return decrypt(privateKey, text, iv);
    }

    /**
     * 生成密码器
     *
     * @param algorithmName 算法名称
     * @param mode          模式
     * @param key           密钥
     * @param iv            加密初始向量
     */
    private Cipher generateCipher(String algorithmName, int mode, byte[] key, String iv) {
        try {
            Cipher cipher = Cipher.getInstance(algorithmName, BouncyCastleProvider.PROVIDER_NAME);
            Key sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
            IvParameterSpec ips = new IvParameterSpec((StringUtils.isBlank(iv) ? INITIAL_VECTOR : iv).getBytes(Coder.DEFAULT_ENCODING));
            cipher.init(mode, sm4Key, ips);
            return cipher;
        } catch (Exception e) {
            throw new CryptoException("SM4 generateCipher error.", e);
        }
    }

    /**
     * 加密
     * 密文长度不固定，会随着被加密字符串长度的变化而变化
     *
     * @param key  密钥（忽略大小写）
     * @param text 待加密字符串
     * @param iv   加密初始向量
     * @return 返回加密字符串
     */
    private String encrypt(String key, String text, String iv) {
        try {
            byte[] keyData = bytesConverter.decode(key);
            byte[] srcData = text.getBytes(StandardCharsets.UTF_8);
            byte[] cipherArray = encrypt(keyData, srcData, iv);
            return bytesConverter.encode(cipherArray);
        } catch (Exception e) {
            throw new CryptoException("SM4 encrypt error.", e);
        }
    }

    /**
     * 加密
     *
     * @param key  key
     * @param data 原始数据
     * @param iv   加密初始向量
     * @return 加密数据 byte[]
     */
    private byte[] encrypt(byte[] key, byte[] data, String iv) {
        try {
            Cipher cipher = generateCipher(ALGORITHM_NAME_CBC_PKCS_5_PADDING, Cipher.ENCRYPT_MODE, key, iv);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new CryptoException("SM4 encrypt error.", e);
        }
    }

    /**
     * 解密
     *
     * @param key        密钥
     * @param cipherText 加密字符串（忽略大小写）
     * @param iv         加密初始向量
     * @return 解密后的字符串
     */
    private String decrypt(String key, String cipherText, String iv) {
        String decryptStr;
        byte[] keyData = bytesConverter.decode(key);
        byte[] cipherData = bytesConverter.decode(cipherText);
        byte[] srcData;
        try {
            srcData = decrypt(keyData, cipherData, iv);
            decryptStr = new String(srcData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CryptoException("SM4 decrypt error.", e);
        }
        return decryptStr;
    }

    /**
     * 解密
     *
     * @param key        密钥
     * @param cipherText 密文
     * @param iv         加密初始向量
     * @return byte[]
     */
    private byte[] decrypt(byte[] key, byte[] cipherText, String iv) {
        try {
            Cipher cipher = generateCipher(ALGORITHM_NAME_CBC_PKCS_5_PADDING, Cipher.DECRYPT_MODE, key, iv);
            return cipher.doFinal(cipherText);
        } catch (Exception e) {
            throw new CryptoException("SM4 decrypt error.", e);
        }
    }

}
