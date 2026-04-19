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
package wang.bigbird.domain.framework.common.crypto.base.enums;

/**
 * AES加密模式
 *
 * @author Bigbird
 */
public enum AesModelEnum {

    /**
     * 电码本模式（Electronic Codebook Book）
     * 是一种基础的加密方式，密文被分割成分组长度相等的块（不足补齐），
     * 然后单独一个个加密，一个个输出组成密文。
     */
    ECB,

    /**
     * 密码分组链接模式（Cipher Block Chaining）
     * 先将明文切分成若干小段，然后每一小段与初始块或者上一段的密文段进行异或运算后，
     * 再与密钥进行加密
     */
    CBC,

    /**
     * 密码反馈模式
     */
    CFB,

    /**
     * 输出反馈模式
     */
    OFB,

    /**
     * 计算器模式
     * 有一个自增的算子，这个算子用密钥加密之后的输出和明文异或的结果得到密文
     */
    CTR;
}
