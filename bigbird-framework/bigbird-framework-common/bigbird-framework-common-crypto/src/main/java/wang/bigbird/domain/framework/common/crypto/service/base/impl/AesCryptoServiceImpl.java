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
import wang.bigbird.domain.framework.common.crypto.base.enums.AesModelEnum;
import wang.bigbird.domain.framework.common.crypto.base.enums.Byte2StringTypeEnum;
import wang.bigbird.domain.framework.common.crypto.base.tool.IBytesConverter;
import wang.bigbird.domain.framework.common.crypto.base.util.BytesUtils;
import wang.bigbird.domain.framework.common.crypto.exception.CryptoException;
import wang.bigbird.domain.framework.common.crypto.service.base.IAesCryptoService;
import wang.bigbird.domain.framework.core.base.tool.Coder;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * AES对称加密解密工具
 *
 * @author Bigbird
 */
@Slf4j
@Setter
@Accessors(chain = true)
public class AesCryptoServiceImpl implements IAesCryptoService {

    /**
     * 解析后的key缓存
     */
    private ConcurrentMap<String, KeyWrapper> keyCache = new ConcurrentHashMap<>();
    /**
     * 是否可缓存
     */
    private boolean cacheEnable;
    /**
     * 字节转换器
     */
    private IBytesConverter bytesConverter = IBytesConverter.getInstance(Byte2StringTypeEnum.BASE64);
    /**
     * 加密模式
     */
    private String model = AesModelEnum.ECB.name();
    /**
     * 填充方式 jdk加密的不支持其他方式的填充
     */
    private String padding = "PKCS5PADDING";
    /**
     * 偏移量
     */
    private static final String INITIAL_VECTOR = "BIRD19841222ctdi";

    /**
     * key封装
     *
     * @param <T>
     */
    class KeyWrapper<T> {
        T key;
    }

    @Override
    public byte[] getKey(@NonNull String key) {
        if (!cacheEnable) {
            return BytesUtils.fromBase64(key);
        }
        KeyWrapper<byte[]> wrapper = keyCache.get(key);
        if (wrapper == null) {
            wrapper = new KeyWrapper<>();
            // 此处相同key会阻塞，直到一个线程将对象放入后返回
            KeyWrapper exist = keyCache.putIfAbsent(key, wrapper);
            if (exist == null) {
                // 首次放入时，返回必然为null，此时key必然需要初始化
                wrapper.key = BytesUtils.fromBase64(key);
            } else {
                wrapper = exist;
                // 多线程时，可能key没有初始化完毕，需要自旋等待
                while (exist.key == null) {
                    log.warn("Waiting for parsing key.");
                }
            }
        }
        return wrapper.key;
    }

    @Override
    public String createKey(@NonNull String seed, @NonNull AesKeyLengthEnum keyLength) {
        return BytesUtils.toBase64(createKeyBytes(seed, keyLength));
    }

    @Override
    public byte[] encrypt(byte[] data, @NonNull String key, String iv) {
        BytesUtils.notEmpty(data);
        Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, key, iv);
        try {
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new CryptoException("Encrypt error.", e);
        }
    }

    @Override
    public String encrypt2String(@NonNull String text, @NonNull String key, String iv) {
        return bytesConverter.encode(encrypt(text.getBytes(), key, iv));
    }

    @Override
    public byte[] decrypt(byte[] data, @NonNull String key, String iv) {
        BytesUtils.notEmpty(data);
        Cipher cipher = getCipher(Cipher.DECRYPT_MODE, key, iv);
        try {
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new CryptoException("Decrypt error.", e);
        }
    }

    @Override
    public String decrypt2String(@NonNull String text, @NonNull String key, String iv) {
        byte[] bytes = bytesConverter.decode(text);
        byte[] data = decrypt(bytes, key, iv);
        return new String(data);
    }

    /**
     * 获得密码器
     *
     * @param mode 构造用于执行加密或者解密的密码器
     * @param key  密钥
     * @param iv   加密初始向量
     * @return 采用指定密钥与执行模式的密码器
     */
    private Cipher getCipher(int mode, String key, String iv) {
        try {
            byte[] keyBytes = getKey(key);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/" + model + "/" + padding);
            if (AesModelEnum.ECB.name().equals(model)) {
                cipher.init(mode, secretKey);
            } else if (AesModelEnum.CBC.name().equals(model)) {
                IvParameterSpec ivParam = new IvParameterSpec((StringUtils.isBlank(iv) ? INITIAL_VECTOR : iv).getBytes(Coder.DEFAULT_ENCODING));
                cipher.init(mode, secretKey, ivParam);
            } else {
                throw new CryptoException("The current encryption mode is not supported.");
            }
            return cipher;
        } catch (Exception e) {
            throw new CryptoException("Error creating cipher.", e);
        }
    }

    private byte[] createKeyBytes(@NonNull String seed, AesKeyLengthEnum keyLength) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(seed.getBytes());
            kgen.init(keyLength.value, secureRandom);
            SecretKey secretKey = kgen.generateKey();
            return secretKey.getEncoded();
        } catch (Exception e) {
            throw new CryptoException("Error generating secret key.", e);
        }
    }

}
