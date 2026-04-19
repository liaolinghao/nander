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
package wang.bigbird.domain.framework.data.oss.base.util;

import net.coobird.thumbnailator.Thumbnails;
import wang.bigbird.domain.framework.core.base.util.StreamUtils;

import java.awt.image.BufferedImage;
import java.io.*;

/**
 * 缩略图工具
 *
 * @author Bigbird
 */
public class ThumbImageUtils {

    /**
     * 生成缩略图
     *
     * @param srcFilePath  源路径，绝对路径
     * @param destFilePath 目标路径
     * @param width        缩略图宽
     * @param height       缩略图高
     * @throws IOException
     */
    public static void generateThumbImage(String srcFilePath, String destFilePath, int width, int height) throws IOException {
        generateThumbImage(new FileInputStream(srcFilePath), destFilePath, width, height);
    }

    /**
     * 生成缩略图
     *
     * @param in           图片输入流
     * @param destFilePath 目标路径
     * @param width        缩略图宽
     * @param height       缩略图高
     * @throws IOException
     */
    public static void generateThumbImage(InputStream in, String destFilePath, int width, int height) throws IOException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(destFilePath);
            Thumbnails
                    .of(in)
                    .size(width, height)
                    .imageType(BufferedImage.TYPE_INT_ARGB)
                    .toOutputStream(out);
        } finally {
            StreamUtils.close(out, in);
        }
    }

}
