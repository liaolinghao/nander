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

import wang.bigbird.domain.framework.core.base.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 基于 SHA-256 的哈希算法
 * <p>
 * HMAC（Hash-based Message Authentication Code，基于哈希的消息认证码），用于验证消息的完整性和真实性。
 * 它结合了密钥和消息，生成一个固定长度的哈希值，确保消息在传输过程中未被篡改且来自合法发送方。
 *
 * @author Bigbird
 */
public class HmacSha256 {

    /**
     * 算法名称
     */
    private static final String HMAC_SHA256 = "HmacSHA256";

    private HmacSha256() {
        throw new IllegalStateException();
    }

    /**
     * 生成 HMACSHA256 签名（十六进制字符串）
     *
     * @param message 待签名的消息
     * @param key     密钥（字符串）
     * @return 十六进制格式的签名
     */
    public static String hash(String message, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        if (message == null || key == null) {
            return null;
        }
        // 将消息和密钥转换为字节数组（使用UTF-8编码）
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        // 生成HMAC字节数组
        byte[] hmacBytes = hash(messageBytes, keyBytes);
        // 转换为十六进制字符串
        return StringUtils.bytesToHex(hmacBytes);
    }

    /**
     * 核心方法：生成 HMACSHA256 字节数组
     *
     * @param messageBytes 消息字节数组
     * @param keyBytes     密钥字节数组
     * @return HMACSHA256 结果字节数组
     */
    private static byte[] hash(byte[] messageBytes, byte[] keyBytes) throws NoSuchAlgorithmException, InvalidKeyException {
        // 创建HMAC密钥规范
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, HMAC_SHA256);
        // 获取HMAC实例
        Mac mac = Mac.getInstance(HMAC_SHA256);
        // 初始化HMAC（传入密钥）
        mac.init(secretKeySpec);
        // 计算HMAC值（处理消息并生成签名）
        return mac.doFinal(messageBytes);
    }

}
