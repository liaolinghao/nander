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
package wang.bigbird.domain.framework.core.base.tool.arithmetic;

import wang.bigbird.domain.framework.core.base.tool.Coder;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import java.io.UnsupportedEncodingException;

/**
 * RC4加密解密算法
 * <p>
 * RC4是一种对称密码算法，它属于对称密码算法中的序列密码（streamcipher，也称为流密码），
 * 它是可变密钥长度，面向字节操作的流密码。RC4的执行速度相当快，它大约是分块密码算法DES的5倍，
 * 是3DES的15倍，且比高级加密算法AES也快很多。RC4算法特点：
 * (1) 算法简洁易于软件实现，加密速度快，安全性比较高；
 * (2) 密钥长度可变，一般用256个字节。
 * <p>
 *
 * @author Bigbird
 */
public class Rc4 {

    /**
     * 密钥长度
     */
    private static final int KEY_LENGTH = 256;

    /**
     * RC4加密，将加密后的数据进行哈希
     *
     * @param data 需要加密的数据
     * @param key  密钥
     * @return 返回加密后的数据
     * @throws UnsupportedEncodingException
     */
    public static String encrypt(String data, String key) throws UnsupportedEncodingException {
        return encrypt(data, key, Coder.DEFAULT_ENCODING);
    }

    /**
     * RC4加密，将加密后的数据进行哈希
     *
     * @param data     需要加密的数据
     * @param key      密钥
     * @param chartSet 编码方式
     * @return 返回加密后的数据
     * @throws UnsupportedEncodingException
     */
    public static String encrypt(String data, String key, String chartSet) throws UnsupportedEncodingException {
        if (data == null || key == null) {
            return null;
        }
        return StringUtils.bytesToHex(encryptByte(data, key, chartSet));
    }

    /**
     * RC4加密
     *
     * @param data     需要加密的数据
     * @param key      密钥
     * @param chartSet 编码方式
     * @return 返回加密后的数据字节数组
     * @throws UnsupportedEncodingException
     */
    public static byte[] encryptByte(String data, String key, String chartSet) throws UnsupportedEncodingException {
        if (data == null || key == null) {
            return null;
        }
        if (chartSet == null || chartSet.isEmpty()) {
            return rc4Base(data.getBytes(), key);
        } else {
            return rc4Base(data.getBytes(chartSet), key);
        }
    }

    /**
     * RC4解密
     *
     * @param data 需要解密的数据
     * @param key  密钥
     * @return 返回解密后的数据
     * @throws UnsupportedEncodingException
     */
    public static String decrypt(String data, String key) throws UnsupportedEncodingException {
        return decrypt(data, key, Coder.DEFAULT_ENCODING);
    }

    /**
     * RC4解密
     *
     * @param data     需要解密的数据
     * @param key      密钥
     * @param chartSet 编码方式
     * @return 返回解密后的数据
     * @throws UnsupportedEncodingException
     */
    public static String decrypt(String data, String key, String chartSet) throws UnsupportedEncodingException {
        if (data == null || key == null) {
            return null;
        }
        return new String(rc4Base(StringUtils.hexToByte(data), key), chartSet);
    }

    /**
     * RC4算法初始化密钥
     *
     * @param key 密钥
     * @return 算法处理后的密钥字节数据
     */
    private static byte[] initKey(String key) {
        byte[] bKey = key.getBytes();
        byte[] state = new byte[KEY_LENGTH];
        for (int i = 0; i < KEY_LENGTH; i++) {
            state[i] = (byte) i;
        }
        int index1 = 0;
        int index2 = 0;
        if (bKey.length == 0) {
            return null;
        }
        for (int i = 0; i < KEY_LENGTH; i++) {
            index2 = ((bKey[index1] & 0xff) + (state[i] & 0xff) + index2) & 0xff;
            byte tmp = state[i];
            state[i] = state[index2];
            state[index2] = tmp;
            index1 = (index1 + 1) % bKey.length;
        }
        return state;
    }

    /**
     * RC4算法处理
     *
     * @param input 源数据
     * @param mKkey 密钥
     * @return RC4算法处理后的字节数据
     */
    private static byte[] rc4Base(byte[] input, String mKkey) {
        int x = 0;
        int y = 0;
        byte[] key = initKey(mKkey);
        int xorIndex;
        byte[] result = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            x = (x + 1) & 0xff;
            y = ((key[x] & 0xff) + y) & 0xff;
            byte tmp = key[x];
            key[x] = key[y];
            key[y] = tmp;
            xorIndex = ((key[x] & 0xff) + (key[y] & 0xff)) & 0xff;
            result[i] = (byte) (input[i] ^ key[xorIndex]);
        }
        return result;
    }

}
