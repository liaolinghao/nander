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

import java.io.UnsupportedEncodingException;

/**
 * Base64编码解码算法
 *
 * @author Bigbird
 */
public class Base64 {

    private Base64() {
        throw new IllegalStateException();
    }

    /**
     * Base64编码
     *
     * @param data 需要编码的数据
     * @return 返回Base64编码后的数据
     * @throws UnsupportedEncodingException
     */
    public static String encode(String data) throws UnsupportedEncodingException {
        return encode(data, Coder.DEFAULT_ENCODING);
    }

    /**
     * Base64编码
     *
     * @param data     需要编码的数据
     * @param chartSet 字符编码方式
     * @return 返回Base64编码后的数据
     * @throws UnsupportedEncodingException
     */
    public static String encode(String data, String chartSet) throws UnsupportedEncodingException {
        if (data == null) {
            return null;
        }
        return java.util.Base64.getEncoder().encodeToString(data.getBytes(chartSet));
    }

    /**
     * Base64解码
     *
     * @param data 需要解码的数据
     * @return 返回Base64解码后的数据
     * @throws UnsupportedEncodingException
     */
    public static String decode(String data) throws UnsupportedEncodingException {
        return decode(data, Coder.DEFAULT_ENCODING);
    }

    /**
     * Base64解码
     *
     * @param data 需要解码的数据
     * @param chartSet 字符编码方式
     * @return 返回Base64解码后的数据
     * @throws UnsupportedEncodingException
     */
    public static String decode(String data, String chartSet) throws UnsupportedEncodingException {
        if (data == null) {
            return null;
        }
        byte[] bs = java.util.Base64.getDecoder().decode(data);
        return new String(bs, chartSet);
    }

}
