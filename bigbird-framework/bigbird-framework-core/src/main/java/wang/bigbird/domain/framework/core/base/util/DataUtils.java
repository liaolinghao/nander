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

import wang.bigbird.domain.framework.core.base.tool.Assert;

import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 数据操作工具类
 *
 * @author Bigbird
 */
public class DataUtils {

    /**
     * 单位进位，中文默认为4位即（万、亿）
     */
    public static int UNIT_STEP = 4;

    /**
     * 单位
     */
    public static String[] CN_UNITS = new String[]{"个", "十", "百", "千", "万",
            "十", "百", "千", "亿", "十", "百", "千", "万"};

    /**
     * 汉字
     */
    public static String[] CN_CHARS = new String[]{"零", "一", "二", "三", "四",
            "五", "六", "七", "八", "九"};

    /**
     * 单精度最小值
     */
    public static final float FLOAT_MIN_VALUE = -1 * Float.MAX_VALUE;

    /**
     * 双精度最小值
     */
    public static final double DOUBLE_MIN_VALUE = -1 * Double.MAX_VALUE;

    /**
     * 默认公差
     */
    private static final double DEFAULT_TOLERANCE = 0.00001;

    /**
     * 小数点
     */
    private static final String DOT = ".";

    /**
     * 百分号
     */
    private static final String PERCENT_SYMBOL = "%";

    /**
     * 数值10
     */
    private static final long TEN = 10;

    /**
     * 随机生成器
     */
    private static final Random random = new Random();

    /**
     * 判断是否约等于0，以默认公差0.00001为标准
     *
     * @param d 待判断数值
     * @return 判断结果
     */
    public static boolean approxZero(double d) {
        return approxEquals(d, 0);
    }

    /**
     * 以默认公差0.00001为标准，判断两个数是否接近
     *
     * @param d1 待比较数值1
     * @param d2 待比较数值2
     * @return 判断结果
     */
    public static boolean approxEquals(double d1, double d2) {
        return approximate(d1, d2, DEFAULT_TOLERANCE);
    }

    /**
     * 判断两个数是否近似，以指定的公差为标准
     *
     * @param d1 待比较数值1
     * @param d2 待比较数值2
     * @param p  两个数之差是否小于p
     * @return 判断结果
     */
    public static boolean approximate(double d1, double d2, double p) {
        return Math.abs(d1 - d2) < Math.abs(p);
    }

    /**
     * 将 double转为string，没有","分割，并且不采用科学计数法
     *
     * @param d 需要四舍五入的数字
     * @param p 最多保留几位小数，默认采取四舍五入的方式
     * @return 格式化后数字串
     */
    public static String double2String(double d, int p) {
        NumberFormat nf = NumberFormat.getInstance(Locale.CHINA);
        // 如果不设置该参数，默认是保留3位
        nf.setMaximumFractionDigits(p);
        return nf.format(d).replaceAll(",", "");
    }

    /**
     * 格式化double数值为对应字符串，严格设置小数点位数为指定位数，若小数位数不足，则补0
     *
     * @param d 需要四舍五入的数字
     * @param p 保留几位小数
     * @return 格式化后数字串
     */
    public static String formatDouble2String(double d, int p) {
        String doubleStr = double2String(d, p);
        StringBuilder sb = new StringBuilder();
        if (doubleStr.contains(DOT)) {
            int index = doubleStr.indexOf(DOT);
            int decimalLength = doubleStr.substring(index + 1).length();
            int plus = p - decimalLength;
            for (int i = 0; i < plus; i++) {
                sb.append(0);
            }
            return doubleStr + sb.toString();
        } else {
            for (int i = 0; i < p; i++) {
                sb.append(0);
            }
            return doubleStr + DOT + sb.toString();
        }
    }

    /**
     * 提供精确的小数位四舍五入处理。
     *
     * @param f 需要四舍五入的数字
     * @param p 小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static float round(float f, int p) {
        if (p < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Float.toString(f));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, p, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    /**
     * 提供精确的小数位四舍五入处理。
     *
     * @param d 需要四舍五入的数字
     * @param p 小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static double round(double d, int p) {
        return round(d, p, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 同上，可采取不同的小数位处理方式
     *
     * @param d            需要四舍五入的数字
     * @param p            小数点后保留几位
     * @param roundingMode 四舍五入方式
     * @return 四舍五入后的结果
     */
    public static double round(double d, int p, int roundingMode) {
        if (p < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(d);
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, p, roundingMode).doubleValue();
    }

    /**
     * 取值在指定的最小值与最大值之间，若超出，则取边界值
     *
     * @param value 期望值
     * @param min   最小值
     * @param max   最大值
     * @return 结果值
     */
    public static double truncate(double value, double min, double max) {
        return Math.min(max, Math.max(min, value));
    }

    /**
     * 生成指定位数的随机数字
     *
     * @param length 数字位数
     * @return 数值
     * @throws Exception 非法参数导致的异常
     */
    public static long getRandomData(int length) throws Exception {
        if (length > 0) {
            return Math.round((Math.random() * 9 + 1)
                    * Math.pow(10, length - 1));
        } else {
            throw new Exception("The length should be larger than 0!");
        }
    }

    /**
     * 返回指定范围内的随机数，包含两边端点
     *
     * @param min 最小值
     * @param max 最大值
     * @return 指定范围内的随机数
     */
    public static int getRandomData(int min, int max) {
        int minValue = Math.min(min, max);
        int maxValue = Math.max(min, max);
        return random.nextInt(maxValue - minValue + 1) + minValue;
    }

    /**
     * 获取指定范围内指定数量的随机数
     *
     * @param num 随机数数量
     * @param min 最小值
     * @param max 最大值
     * @return 随机数集合
     */
    public static List<Integer> getRandomNum(int num, int min, int max) {
        int numCount = max - min + 1;
        List<Integer> randomNum = new ArrayList<>();
        // 要获取随机数的数量小于等于总数量，才执行获取操作，以防止陷入死循环
        if (num < numCount) {
            int count = 0;
            while (count < num) {
                int number = getRandomData(min, max);
                if (!randomNum.contains(number)) {
                    randomNum.add(number);
                    count++;
                }
            }
        } else if (num == numCount) {
            for (int i = min; i <= max; i++) {
                randomNum.add(i);
            }
        }
        return randomNum;
    }

    /**
     * 按照四舍五入计算方式执行单精度转整型
     *
     * @param f 待处理数值
     * @return 处理数值结果
     */
    public static int floatToInt(float f) {
        if (f > 0) {
            return (int) (f + 0.5);
        } else if (f < 0) {
            return (int) (f - 0.5);
        } else {
            return 0;
        }
    }

    /**
     * 按照四舍五入计算方式执行双精度转整型
     *
     * @param d 待处理数值
     * @return 处理数值结果
     */
    public static int doubleToInt(double d) {
        if (d > 0) {
            return (int) (d + 0.5);
        } else if (d < 0) {
            return (int) (d - 0.5);
        } else {
            return 0;
        }
    }

    /**
     * 单精度转换成双精度会出现精度丢失，该方法是为了防止精度丢失
     *
     * @param value 待处理数值
     * @return 处理数值结果
     */
    public static double floatToDouble(float value) {
        return Double.parseDouble(String.valueOf(value));
    }

    /**
     * 将百分数解析为对应的float型小数
     *
     * @param percent 百分数
     * @return 处理数值结果
     * @throws Exception 参数错误时抛出异常
     */
    public static float parsePercent(String percent) throws Exception {
        Assert.notNull(percent, "The parameter percent is null.");
        if (percent.endsWith(PERCENT_SYMBOL)) {
            percent = percent.substring(0, percent.lastIndexOf(PERCENT_SYMBOL));
            return Float.parseFloat(percent) / 100;
        }
        throw new Exception("Bad format percent!");
    }

    /**
     * 获取布尔值
     *
     * @param str 待处理字符串
     * @return 结果值
     */
    public static boolean getBoolean(String str) {
        return Boolean.parseBoolean(str);
    }

    /**
     * 获取float值
     *
     * @param str 待处理字符串
     * @return 结果值
     */
    public static float getFloat(String str) {
        return Float.parseFloat(str);
    }

    /**
     * 获取double值
     *
     * @param str 待处理字符串
     * @return 结果值
     */
    public static double getDouble(String str) {
        return Double.parseDouble(str);
    }

    /**
     * 获取int值
     *
     * @param str 待处理字符串
     * @return 结果值
     */
    public static int getInteger(String str) {
        return Integer.parseInt(str);
    }

    /**
     * 获取long值
     *
     * @param str 待处理字符串
     * @return 结果值
     */
    public static long getLong(String str) {
        return Long.parseLong(str);
    }

    /**
     * 检测该字符串是否是不良的数值格式，指该字符串能够被数值类解析为对应数值，但是格式不良好 该方法主要用于数值输入组件的数值验证上
     *
     * @param numStr 待判断字符串
     * @return 是否不良格式
     */
    public static boolean isBadFormat(String numStr) {
        Assert.notNull(numStr, "The parameter numStr is null.");
        List<String> badFormatList = new ArrayList<>(Arrays.asList("-0", "-0.", "0."));
        // 不良的整数格式，如：01;-01
        // 不良的小数格式，如：.01;-.01
        // 不良的小数格式，如：01.;-01.01
        // 不良的小数格式，如：11.;-11.
        return badFormatList.contains(numStr) || Pattern.matches("-?0\\d+", numStr)
                || Pattern.matches("-?\\.\\d+", numStr)
                || Pattern.matches("-?0\\d+\\.\\d*", numStr)
                || Pattern.matches("-?\\d+\\.", numStr);
    }

    /**
     * 判断字符串是否能转换成数值
     *
     * @param text 待判断字符串
     * @return 是否数值
     */
    public static boolean isNumber(String text) {
        try {
            Double.valueOf(text);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 是否是正整数
     *
     * @param original 待判断字符串
     * @return 判断结果
     */
    public static boolean isPositiveInteger(String original) {
        return Integer.parseInt(original) >= 0;
    }

    /**
     * 是否是负整数
     *
     * @param original 待判断字符串
     * @return 判断结果
     */
    public static boolean isNegativeInteger(String original) {
        return Integer.parseInt(original) < 0;
    }

    /**
     * 判断一个整数是否属于质数
     *
     * @param n 待判断整数
     * @return 判断结果
     */
    public static boolean isPrimes(int n) {
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取数字集合中的最大值
     *
     * @param numbs 数字集合
     * @return 最大数字
     */
    public static Number max(Number... numbs) {
        Assert.notNull(numbs, "The parameter numbs is null.");
        Number max = numbs[0];
        for (Number num : numbs) {
            if (num.doubleValue() > max.doubleValue()) {
                max = num;
            }
        }
        return max;
    }

    /**
     * 获取数字集合中的最小值
     *
     * @param numbs 数字集合
     * @return 最小数字
     */
    public static Number min(Number... numbs) {
        Assert.notNull(numbs, "The parameter numbs is null.");
        Number min = numbs[0];
        for (Number num : numbs) {
            if (num.doubleValue() < min.doubleValue()) {
                min = num;
            }
        }
        return min;
    }

    /**
     * 像素转毫米
     *
     * @param pixel 像素值
     * @return 毫米值
     */
    public static double pixel2Millimetre(int pixel) {
        int pixelPerInch = Toolkit.getDefaultToolkit().getScreenResolution();
        return pixel * 25.4 / pixelPerInch;
    }

    /**
     * 毫米转像素
     *
     * @param millimetre 毫米值
     * @return 像素值
     */
    public static int millimetre2Pixel(int millimetre) {
        int pixelPerInch = Toolkit.getDefaultToolkit().getScreenResolution();
        return doubleToInt(millimetre * pixelPerInch / 25.4);
    }

    /**
     * 计算两个数的海明距离
     *
     * @param x 数值1
     * @param y 数值2
     * @return 数值间海明距离
     */
    public static int hammingDistance(int x, int y) {
        int dist = 0;
        int val = x ^ y;
        while (val > 0) {
            ++dist;
            val &= val - 1;
        }
        return dist;
    }

    /**
     * 将阿拉伯数字转换为中文数字，比如：123 -》一二三
     *
     * @param srcNum 阿拉伯数字
     * @return 中文数字
     */
    public static String convert2CnNum(int srcNum) {
        StringBuilder desCnNum = new StringBuilder();
        if (srcNum == 0) {
            desCnNum.append("零");
        } else {
            boolean negative = srcNum < 0;
            srcNum = Math.abs(srcNum);
            int singleDigit;
            while (srcNum > 0) {
                singleDigit = srcNum % 10;
                desCnNum.append(CN_CHARS[singleDigit]);
                srcNum /= 10;
            }
            if (negative) {
                desCnNum.append("负");
            }
        }
        return desCnNum.reverse().toString();
    }

    /**
     * 数值转换为中文字符串（口语化）
     *
     * @param num          需要转换的数值
     * @param isColloquial 是否口语化。例如12转换为'十二'而不是'一十二'。
     * @return 口语化表达串，如果输入num不是数值，则原样返回
     */
    public static String convert2Cn(String num, boolean isColloquial) {
        if (!isNumber(num)) {
            return num;
        }
        int integer, decimal = 0;
        StringBuilder stringBuilder = new StringBuilder(32);
        String[] splitNum = num.split("\\.");
        // 整数部分
        integer = Integer.parseInt(splitNum[0]);
        if (splitNum.length > 1) {
            // 小数部分
            decimal = Integer.parseInt(splitNum[1]);
        }
        String[] result1 = convert(integer, isColloquial);
        for (String str1 : result1) {
            stringBuilder.append(str1);
        }
        if (decimal == 0) {
            // 小数部分为0时
            return stringBuilder.toString();
        }
        // 例如5.32，小数部分展示三二，而不是三十二
        String result2 = convert2CnNum(decimal);
        stringBuilder.append("点");
        stringBuilder.append(result2);
        return stringBuilder.toString();
    }

    /**
     * 数值转换为中文字符串，如：2转化为二
     *
     * @param num 需要转换的数值
     * @return 数值对应的中文字符串
     */
    public static String convert2Cn(long num) {
        return convert2Cn(num, false);
    }

    /**
     * 数值转换为中文字符串
     *
     * @param num          需要转换的数值
     * @param isColloquial 是否口语化，例如：12转换为'十二'而不是'一十二'。
     * @return 数值对应的中文字符串
     */
    public static String convert2Cn(long num, boolean isColloquial) {
        String[] result = convert(num, isColloquial);
        StringBuilder stringBuilder = new StringBuilder(32);
        for (String str : result) {
            stringBuilder.append(str);
        }
        return stringBuilder.toString();
    }

    /**
     * 将数值转换为中文
     *
     * @param num          需要转换的数值
     * @param isColloquial 是否口语化，例如：12转换为'十二'而不是'一十二'。
     * @return 数值对应的中文字符串
     */
    public static String[] convert(long num, boolean isColloquial) {
        boolean negative = num < 0;
        num = Math.abs(num);
        if (num < TEN) {
            // 10以下直接返回对应汉字
            if (negative) {
                return new String[]{"负", CN_CHARS[(int) num]};
            } else {
                return new String[]{CN_CHARS[(int) num]};
            }
        }
        char[] chars = String.valueOf(num).toCharArray();
        if (chars.length > CN_UNITS.length) {
            // 超过单位表示范围的返回空
            return new String[]{};
        }
        boolean isLastUnitStep = false;
        // 记录上次单位进位
        ArrayList<String> cnChars = new ArrayList<>(chars.length * 2);
        // 创建数组，将数字填入单位对应的位置
        for (int pos = chars.length - 1; pos >= 0; pos--) {
            // 从低位向高位循环
            char ch = chars[pos];
            // ascii2int 汉字
            String cnChar = CN_CHARS[ch - '0'];
            // 对应的单位坐标
            int unitPos = chars.length - pos - 1;
            // 单位
            String cnUnit = CN_UNITS[unitPos];
            // 是否为0
            boolean isZero = (ch == '0');
            // 是否低位为0
            boolean isZeroLow = (pos + 1 < chars.length && chars[pos + 1] == '0');
            // 当前位是否需要单位进位
            boolean isUnitStep = (unitPos >= UNIT_STEP && (unitPos % UNIT_STEP == 0));
            if (isUnitStep && isLastUnitStep) {
                // 去除相邻的上一个单位进位
                int size = cnChars.size();
                cnChars.remove(size - 1);
                if (!CN_CHARS[0].equals(cnChars.get(size - 2))) {
                    // 补0
                    cnChars.add(CN_CHARS[0]);
                }
            }
            if (isUnitStep || !isZero) {
                // 单位进位(万、亿)，或者非0时加上单位
                cnChars.add(cnUnit);
                isLastUnitStep = isUnitStep;
            }
            boolean isZeroLowOrUnitStep = isZeroLow || isUnitStep;
            if (isZero && isZeroLowOrUnitStep) {
                // 当前位为0低位为0，或者当前位为0并且为单位进位时进行省略
                continue;
            }
            cnChars.add(cnChar);
            isLastUnitStep = false;
        }
        Collections.reverse(cnChars);
        // 清除最后一位的0
        int chSize = cnChars.size();
        String chEnd = cnChars.get(chSize - 1);
        if (CN_CHARS[0].equals(chEnd) || CN_UNITS[0].equals(chEnd)) {
            cnChars.remove(chSize - 1);
        }
        // 口语化处理
        if (isColloquial) {
            String chFirst = cnChars.get(0);
            String chSecond = cnChars.get(1);
            if (chFirst.equals(CN_CHARS[1]) && chSecond.startsWith(CN_UNITS[1])) {
                // 是否以'一'开头，紧跟'十'
                cnChars.remove(0);
            }
        }
        if (negative) {
            cnChars.add(0, "负");
        }
        return cnChars.toArray(new String[]{});
    }

    /**
     * 根据基因因子生成基因id
     *
     * @param primitiveId 原始ID
     * @param factor      基因因子
     * @return 采用基因法对原始ID混淆后的ID
     */
    public static long geneId(long primitiveId, long factor) {
        long sid = (primitiveId & 0xff000000);
        sid += (primitiveId & 0x0000ff00) << 8;
        sid += (primitiveId & 0x00ff0000) >> 8;
        sid += (primitiveId & 0x0000000f) << 4;
        sid += (primitiveId & 0x000000f0) >> 4;
        // 加入factor基因
        sid ^= factor;
        return sid;
    }

    /**
     * 基因id还原为原始ID
     *
     * @param uid    基因混淆后的ID
     * @param factor 基因因子
     * @return 基因反编译后还原的原始ID值
     */
    public static long restoreId(long uid, long factor) {
        uid ^= factor;
        long primitiveId = (uid & 0xff000000);
        primitiveId += (uid & 0x00ff0000) >> 8;
        primitiveId += (uid & 0x0000ff00) << 8;
        primitiveId += (uid & 0x000000f0) >> 4;
        primitiveId += (uid & 0x0000000f) << 4;
        return primitiveId;
    }

}
