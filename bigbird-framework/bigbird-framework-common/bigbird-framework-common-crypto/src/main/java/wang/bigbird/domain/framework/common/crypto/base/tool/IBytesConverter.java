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
package wang.bigbird.domain.framework.common.crypto.base.tool;

import lombok.NonNull;
import wang.bigbird.domain.framework.common.crypto.base.enums.Byte2StringTypeEnum;
import wang.bigbird.domain.framework.common.crypto.base.util.BytesUtils;
import wang.bigbird.domain.framework.common.crypto.exception.CryptoException;

/**
 * 字节数组转换器
 *
 * @author Bigbird
 */
public interface IBytesConverter {

    /**
     * 根据字节数组转换为字符串的方式获取对应转换器
     *
     * @param type 字节数组转换为字符串的转换方式
     * @return 字节数组转换器
     */
    static IBytesConverter getInstance(@NonNull Byte2StringTypeEnum type) {
        switch (type) {
            case HEX_LOWER:
                return new IBytesConverter() {
                    @Override
                    public String encode(byte... bytes) {
                        return BytesUtils.toHexString(bytes).toLowerCase();
                    }

                    @Override
                    public byte[] decode(String str) {
                        return BytesUtils.fromHexString(str.toLowerCase());
                    }
                };
            case HEX_UPPER:
                return new IBytesConverter() {
                    @Override
                    public String encode(byte... bytes) {
                        return BytesUtils.toHexString(bytes).toUpperCase();
                    }

                    @Override
                    public byte[] decode(String str) {
                        return BytesUtils.fromHexString(str.toLowerCase());
                    }
                };
            case BASE64:
                return new IBytesConverter() {
                    @Override
                    public String encode(byte... bytes) {
                        return BytesUtils.toBase64(bytes);
                    }

                    @Override
                    public byte[] decode(String str) {
                        return BytesUtils.fromBase64(str);
                    }
                };
            default:
                throw new CryptoException("Unsupported type:" + type);
        }
    }

    /**
     * 编码
     *
     * @param bytes 将字节数组转换为字符串
     * @return 字符串
     */
    String encode(byte... bytes);

    /**
     * 解码
     *
     * @param str 字符串
     * @return 将字符串转换为字节数组
     */
    byte[] decode(String str);

}
