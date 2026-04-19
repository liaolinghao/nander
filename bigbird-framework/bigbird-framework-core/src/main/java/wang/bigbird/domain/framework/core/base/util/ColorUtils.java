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

import java.awt.*;

/**
 * 颜色操作的一些常用方法
 *
 * @author Bigbird
 */
public class ColorUtils {

    /**
     * 颜色最大值
     */
    private static final int MAX_COLOR_VALUE = 255;
    /**
     * 颜色最小值
     */
    private static final int MIN_COLOR_VALUE = 0;

    /**
     * 颜色对象转换为十六进制字符串
     *
     * @param color 颜色
     * @return 颜色字符串表达式
     */
    public static String color2String(Color color) {
        String r = Integer.toHexString(color.getRed());
        r = r.length() < 2 ? ('0' + r) : r;
        String b = Integer.toHexString(color.getBlue());
        b = b.length() < 2 ? ('0' + b) : b;
        String g = Integer.toHexString(color.getGreen());
        g = g.length() < 2 ? ('0' + g) : g;
        return '#' + r + b + g;
    }

    /**
     * 将十六进制字符串转换为颜色对象
     *
     * @param str 颜色字符串表达式
     * @return 颜色
     */
    public static Color string2Color(String str) {
        int i = Integer.parseInt(str.substring(1), 16);
        return new Color(i);
    }

    /**
     * 随机生成一个颜色
     *
     * @param fc 颜色起始值
     * @param tc 颜色结束值
     * @return 颜色
     */
    public static Color randomColor(int fc, int tc) {
        // 规范化输入范围
        fc = Math.max(MIN_COLOR_VALUE, Math.min(fc, MAX_COLOR_VALUE));
        tc = Math.max(MIN_COLOR_VALUE, Math.min(tc, MAX_COLOR_VALUE));
        // 确保范围有效
        if (tc == fc) {
            tc = Math.min(fc + 10, MAX_COLOR_VALUE);
        } else if (tc < fc) {
            // 交换值使tc >= fc
            int temp = tc;
            tc = fc;
            fc = temp;
        }
        int r = DataUtils.getRandomData(fc, tc);
        int g = DataUtils.getRandomData(fc, tc);
        int b = DataUtils.getRandomData(fc, tc);
        return new Color(r, g, b);
    }

    /**
     * 反转颜色
     *
     * @param c 原始颜色
     * @return 反转后颜色
     */
    public static Color reverseColor(Color c) {
        return new Color(MAX_COLOR_VALUE - c.getRed(), MAX_COLOR_VALUE - c.getGreen(),
                MAX_COLOR_VALUE - c.getBlue());
    }

    /**
     * 弱化颜色
     *
     * @param c    原始颜色
     * @param rate 弱化比率(0-1)
     * @return 弱化后的颜色
     */
    public static Color weakColor(Color c, double rate) {
        rate = 1 + rate;
        double red = c.getRed() * rate;
        double green = c.getGreen() * rate;
        double blue = c.getBlue() * rate;
        if (red > MAX_COLOR_VALUE) {
            red = MAX_COLOR_VALUE;
        }
        if (green > MAX_COLOR_VALUE) {
            green = MAX_COLOR_VALUE;
        }
        if (blue > MAX_COLOR_VALUE) {
            blue = MAX_COLOR_VALUE;
        }
        return new Color((int) red, (int) green, (int) blue);
    }

}
