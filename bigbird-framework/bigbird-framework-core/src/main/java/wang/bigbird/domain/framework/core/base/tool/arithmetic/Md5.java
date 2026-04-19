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

import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.tool.Coder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5摘要算法
 *
 * @author Bigbird
 */
public class Md5 {

    private Md5() {
        throw new IllegalStateException();
    }

    /**
     * 生成文本的MD5摘要（32位十六进制）
     *
     * @param data 需要生成摘要的数据
     * @return 32位十六进制的MD5摘要
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String digest(String data) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        if (data == null) {
            return null;
        }
        // 获取MD5消息摘要实例
        MessageDigest md = MessageDigest.getInstance("MD5");
        // 更新摘要内容（将字符串转换为字节数组，使用UTF-8编码）
        md.update(data.getBytes(Coder.DEFAULT_ENCODING));
        // 计算摘要，得到16字节的哈希值
        byte[] digest = md.digest();
        // 将字节数组转换为32位十六进制字符串
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            // 转换为两位十六进制，不足两位前面补0
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 生成文件的MD5摘要（32位十六进制）
     *
     * @param file 输入文件
     * @return 32位十六进制的MD5摘要
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static String digest(File file) throws IOException, NoSuchAlgorithmException {
        Assert.isFalse(file == null || !file.exists() || !file.isFile(), "The file is invalid.");
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 8KB缓冲区
            byte[] buffer = new byte[8192];
            int length;
            // 分块读取文件并更新摘要
            while ((length = fis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            // 转换为十六进制字符串
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
    }

}
