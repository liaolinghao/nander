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

import wang.bigbird.domain.framework.core.base.constant.CommonConstants;

import java.io.FileOutputStream;
import java.util.Base64;

/**
 * 音频操作工具类
 *
 * @author Bigbird
 */
public class AudioUtils {

    /**
     * MP3 的 Base64 字符串 直接保存成本地 MP3 文件
     *
     * @param base64Str MP3的Base64字符串
     * @param savePath  本地MP3文件地址
     * @throws Exception
     */
    public static void saveMp3Base64ToFile(String base64Str, String savePath) throws Exception {
        // 自动去掉前端前缀（例如 data:audio/mp3;base64,开头）
        if (base64Str.contains(CommonConstants.COMMA)) {
            base64Str = base64Str.split(",")[1];
        }
        // 纯JDK解码 Base64 → 字节数组
        byte[] mp3Data = Base64.getDecoder().decode(base64Str);
        // 直接写入文件 → 就是正常可播放的 MP3
        try (FileOutputStream fos = new FileOutputStream(savePath)) {
            fos.write(mp3Data);
        }
    }


}
