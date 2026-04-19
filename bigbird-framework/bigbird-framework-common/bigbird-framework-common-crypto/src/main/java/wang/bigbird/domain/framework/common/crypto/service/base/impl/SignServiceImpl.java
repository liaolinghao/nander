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
import lombok.ToString;
import lombok.experimental.Accessors;
import org.bouncycastle.crypto.digests.SM3Digest;
import wang.bigbird.domain.framework.common.crypto.base.enums.SignTypeEnum;
import wang.bigbird.domain.framework.common.crypto.base.tool.IBytesConverter;
import wang.bigbird.domain.framework.common.crypto.exception.CryptoException;
import wang.bigbird.domain.framework.common.crypto.service.base.ISignService;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 签名服务
 *
 * @author Bigbird
 */
@Setter
@Accessors(chain = true)
@ToString
public class SignServiceImpl implements ISignService {
    /**
     * 字节转换
     */
    private IBytesConverter bytesConverter;
    /**
     * 签名算法
     */
    private SignTypeEnum signType;

    public SignServiceImpl() {

    }

    public SignServiceImpl(IBytesConverter bytesConverter, SignTypeEnum signType) {
        this.bytesConverter = bytesConverter;
        this.signType = signType;
    }

    @Override
    public String sign(String text) {
        return sign(Charset.defaultCharset(), text);
    }

    @Override
    public String sign(@NonNull Charset charset, @NonNull String text) {
        byte[] bytes = text.getBytes(charset);
        try {
            byte[] resBytes;
            switch (signType) {
                case SM3:
                    resBytes = digestBySM3(bytes);
                    break;
                default:
                    resBytes = digest(bytes, signType.getCode());
            }
            return bytesConverter.encode(resBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("No such algorithm.", e);
        }
    }

    @Override
    public boolean verify(String sign, String text) {
        return verify(Charset.defaultCharset(), sign, text);
    }

    @Override
    public boolean verify(@NonNull Charset charset, @NonNull String sign, @NonNull String text) {
        return sign.equals(sign(charset, text));
    }

    /**
     * SM3生成摘要
     *
     * @param bytes
     * @return
     */
    private byte[] digestBySM3(byte[] bytes) {
        SM3Digest digest = new SM3Digest();
        digest.update(bytes, 0, bytes.length);
        byte[] result = new byte[digest.getDigestSize()];
        digest.doFinal(result, 0);
        return result;
    }

    /**
     * 其他常规算法生成摘要
     *
     * @param bytes
     * @param algorithm
     * @return
     * @throws NoSuchAlgorithmException
     */
    private byte[] digest(byte[] bytes, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        messageDigest.update(bytes);
        return messageDigest.digest();
    }

}
