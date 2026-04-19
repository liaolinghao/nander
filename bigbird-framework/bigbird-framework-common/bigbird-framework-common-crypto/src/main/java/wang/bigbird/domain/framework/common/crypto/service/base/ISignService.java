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
package wang.bigbird.domain.framework.common.crypto.service.base;

import java.nio.charset.Charset;

/**
 * 签名接口
 *
 * @author Bigbird
 */
public interface ISignService {

    /**
     * 签名
     *
     * @param text 待签名的字符串
     * @return 返回16进制字符串或者base64
     */
    String sign(String text);

    /**
     * 签名
     *
     * @param charset 将字符串转换为字节数组的编码方式
     * @param text    待签名的字符串
     * @return 返回16进制字符串或者base64
     */
    String sign(Charset charset, String text);

    /**
     * 校验签名
     *
     * @param sign 签名信息
     * @param text 待校验字符串
     * @return 校验结果是否匹配
     */
    boolean verify(String sign, String text);

    /**
     * 校验签名
     *
     * @param charset 将字符串转换为字节数组的编码方式
     * @param sign    签名信息
     * @param text    待校验字符串
     * @return 校验结果是否匹配
     */
    boolean verify(Charset charset, String sign, String text);
}
