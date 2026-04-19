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
package wang.bigbird.domain.framework.common.crypto.base.util;

import lombok.NonNull;

import java.util.Base64;

/**
 * 字节数组处理工具
 *
 * @author Bigbird
 */
public class BytesUtils {

    /**
     * 一个字节对应2位16进制字符数
     */
    private static final int HEX_DIGITS_PER_BYTE = 2;

    private BytesUtils() {
        throw new IllegalStateException();
    }

    /**
     * 字节数组转换成16进制字符串
     *
     * @param bytes 字节数组
     * @return 16进制字符串
     */
    public static String toHexString(byte... bytes) {
        notEmpty(bytes);
        StringBuilder builder = new StringBuilder(bytes.length * HEX_DIGITS_PER_BYTE);
        for (byte b : bytes) {
            int temp = b & 0xff;
            if (temp < 0x10) {
                builder.append("0");
            }
            builder.append(Integer.toHexString(temp));
        }
        return builder.toString();
    }

    /**
     * 16进制字符串转换为字节数组
     *
     * @param text 16进制字符串
     * @return 字节数组
     */
    public static byte[] fromHexString(@NonNull String text) {
        if (text.length() == 0) {
            return new byte[0];
        }
        int len = text.length();
        byte[] bs = new byte[len / HEX_DIGITS_PER_BYTE];
        for (int i = 0; i < len; i += HEX_DIGITS_PER_BYTE) {
            bs[i / HEX_DIGITS_PER_BYTE] = (byte) ((Character.digit(text.charAt(i), 16) << 4) + Character.digit(text.charAt(i + 1), 16));
        }
        return bs;
    }

    /**
     * 字节数组转换成base64字符串
     *
     * @param bytes 字节数组
     * @return base64字符串
     */
    public static String toBase64(byte... bytes) {
        notEmpty(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * base64字符串转换为字节数组
     *
     * @param text base64字符串
     * @return 字节数组
     */
    public static byte[] fromBase64(@NonNull String text) {
        return Base64.getDecoder().decode(text);
    }

    /**
     * 校验
     *
     * @param bytes 字节数组
     */
    public static void notEmpty(byte... bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new NullPointerException("The bytes must not be null or empty.");
        }
    }

}
