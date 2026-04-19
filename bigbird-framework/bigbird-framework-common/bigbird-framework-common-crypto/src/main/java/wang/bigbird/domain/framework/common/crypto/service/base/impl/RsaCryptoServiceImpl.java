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
import wang.bigbird.domain.framework.common.crypto.base.enums.Byte2StringTypeEnum;
import wang.bigbird.domain.framework.common.crypto.base.enums.RsaKeyBitsEnum;
import wang.bigbird.domain.framework.common.crypto.base.tool.IBytesConverter;
import wang.bigbird.domain.framework.common.crypto.base.util.BytesUtils;
import wang.bigbird.domain.framework.common.crypto.domain.bo.KeyPairBO;
import wang.bigbird.domain.framework.common.crypto.exception.CryptoException;
import wang.bigbird.domain.framework.common.crypto.service.base.IRsaCryptoService;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA非对称加密解密工具
 * 暂时只支持2048位的RSA/ECB/PKCS1Padding加密解密
 *
 * @author Bigbird
 */
@Setter
@Accessors(chain = true)
public class RsaCryptoServiceImpl implements IRsaCryptoService {

    /**
     * 字节转换器
     */
    private IBytesConverter bytesConverter = IBytesConverter.getInstance(Byte2StringTypeEnum.BASE64);
    /**
     * 最大加密长度
     */
    private Integer maxEncryptBytes = 245;
    /**
     * 最大解密长度
     */
    private Integer maxDecryptBytes = 256;

    @Override
    public byte[] encrypt(byte[] data, @NonNull String publicKey, String iv) {
        BytesUtils.notEmpty(data);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] keyBytes = BytesUtils.fromBase64(publicKey);
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey key = keyFactory.generatePublic(x509KeySpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            int offSet = 0;
            for (int i = 0; data.length - offSet > 0; offSet = i * maxEncryptBytes) {
                byte[] cache;
                if (data.length - offSet > maxEncryptBytes) {
                    cache = cipher.doFinal(data, offSet, maxEncryptBytes);
                } else {
                    cache = cipher.doFinal(data, offSet, data.length - offSet);
                }
                out.write(cache, 0, cache.length);
                ++i;
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new CryptoException("Encrypt error.", e);
        }
    }

    @Override
    public String encrypt2String(@NonNull String text, @NonNull String publicKey, String iv) {
        return bytesConverter.encode(encrypt(text.getBytes(), publicKey, iv));
    }

    @Override
    public byte[] decrypt(byte[] data, @NonNull String privateKey, String iv) {
        BytesUtils.notEmpty(data);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] keyBytes = BytesUtils.fromBase64(privateKey);
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey key = keyFactory.generatePrivate(pkcs8KeySpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            int inputLen = data.length;
            int offSet = 0;
            for (int i = 0; inputLen - offSet > 0; offSet = i * maxDecryptBytes) {
                byte[] cache;
                if (inputLen - offSet > maxDecryptBytes) {
                    cache = cipher.doFinal(data, offSet, maxDecryptBytes);
                } else {
                    cache = cipher.doFinal(data, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                ++i;
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new CryptoException("Decrypt error.", e);
        }
    }

    @Override
    public String decrypt2String(@NonNull String text, @NonNull String privateKey, String iv) {
        byte[] data = bytesConverter.decode(text);
        return new String(decrypt(data, privateKey, iv));
    }

    @Override
    public KeyPairBO createKeyPair(@NonNull RsaKeyBitsEnum rsaKeyBits) {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(rsaKeyBits.keyBits);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            return new KeyPairBO()
                    .setPublicKey(BytesUtils.toBase64(keyPair.getPublic().getEncoded()))
                    .setPrivateKey(BytesUtils.toBase64(keyPair.getPrivate().getEncoded()));
        } catch (Exception e) {
            throw new CryptoException("Create RSA key pair failed.", e);
        }
    }

}
