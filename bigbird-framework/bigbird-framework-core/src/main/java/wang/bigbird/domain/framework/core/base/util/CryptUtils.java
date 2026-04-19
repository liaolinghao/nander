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
package wang.bigbird.domain.framework.core.base.util;

import wang.bigbird.domain.framework.core.base.tool.arithmetic.Rc4;

import java.io.UnsupportedEncodingException;

import static wang.bigbird.domain.framework.core.base.constant.CommonConstants.*;

/**
 * 自定义加解密工具类
 *
 * @author Bigbird
 */
public class CryptUtils {

    /**
     * 判断数据值是否为加密值
     *
     * @param value 数据值
     * @return 是否为加密值
     */
    public static boolean isEnc(String value) {
        return value.startsWith(ENC + PARENTHESIS_START) && value.endsWith(PARENTHESIS_END);
    }

    /**
     * 采用自定义算法将数据明文转换为密文
     *
     * @param value 数据明文
     * @param key   加密密钥
     * @return 数据密文
     * @throws UnsupportedEncodingException
     */
    public static String encrypt(String value, String key) throws UnsupportedEncodingException {
        return StringUtils.joinStr(ENC, PARENTHESIS_START, Rc4.encrypt(value, key), PARENTHESIS_END);
    }

    /**
     * 采用自定义算法将数据密文转换为明文
     *
     * @param value 数据密文
     * @param key   解密密钥
     * @return 数据明文
     */
    public static String decrypt(String value, String key) {
        if (!isEnc(value)) {
            return value;
        }
        String data = value.substring((ENC + PARENTHESIS_START).length(), value.length() - PARENTHESIS_END.length());
        try {
            return Rc4.decrypt(data, key);
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

}
