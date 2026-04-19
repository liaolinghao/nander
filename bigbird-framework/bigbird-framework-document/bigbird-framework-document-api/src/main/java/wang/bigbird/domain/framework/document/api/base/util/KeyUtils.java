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
package wang.bigbird.domain.framework.document.api.base.util;

/**
 * 提供文档模版中关于键的通用操作方法
 *
 * @author Bigbird
 */
public class KeyUtils {

    /**
     * 标签起始符
     */
    public static String TAG_START = "{{";
    /**
     * 标签结束符
     */
    public static String TAG_END = "}}";
    /**
     * BMP文件后缀
     */
    public static String IMAGE_BMP = ".bmp";
    /**
     * PNG文件后缀
     */
    public static String IMAGE_PNG = ".png";
    /**
     * JPG文件后缀
     */
    public static String IMAGE_JPG = ".jpg";
    /**
     * JPEG文件后缀
     */
    public static String IMAGE_JPEG = ".jpeg";

    /**
     * 拆除key两边的标签符
     *
     * @param key 模板中定义的标签
     * @return 拆除标签符后的标签串
     */
    public static String filterTag(String key) {
        if (key.startsWith(TAG_START) && key.endsWith(TAG_END)) {
            return key.substring(TAG_START.length(), key.length() - TAG_END.length());
        }
        return key;
    }

}
