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

import wang.bigbird.domain.framework.core.base.util.DataUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 字符串混淆算法
 * <p>
 * 原理为：
 * 1、字符池"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
 * 2、原始字符串：由英文字母+数字构成；
 * 3、获取1到9之间的一个随机值作为偏移值；
 * 4、解析原始字符串中的每个字符，按照（字符索引值+偏移值）% 62 = 新索引 在字符池中找到目标字符；
 * 5、将每个目标字符拼接起来得到偏移字符串；
 * 6、构造字符数1到9之间的头部随机串
 * 7、构造字符数1到9之间的尾部随机串
 * 8、固定三位数字干扰特征串：头部随机串长度 + 偏移值 + 尾部随机串长度
 * 9、目标字符串：干扰特征串+头部随机串+偏移字符串+尾部字符串
 *
 * @author Bigbird
 */
public class StringObfuscator {

    public static final String POOL = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final Map<Character, Integer> CHAR_TO_IDX = new HashMap<>();

    private static final int FEATURE_LENGTH = 3;

    static {
        for (int i = 0; i < POOL.length(); i++) {
            CHAR_TO_IDX.put(POOL.charAt(i), i);
        }
    }

    /**
     * 编码
     *
     * @param origin 原始字符串（字母+数字）
     * @return 最终干扰后的目标字符串
     */
    public static String encode(String origin) {
        if (origin == null || origin.isEmpty()) {
            return origin;
        }
        // 全局偏移
        int offset = DataUtils.getRandomData(1, 9);
        // 头部噪声长度
        int hLen = DataUtils.getRandomData(1, 9);
        // 尾部噪声长度
        int tLen = DataUtils.getRandomData(1, 9);
        // 生成偏移主体串
        StringBuilder mainSb = new StringBuilder();
        for (char c : origin.toCharArray()) {
            Integer idx = CHAR_TO_IDX.get(c);
            if (idx == null) {
                throw new IllegalArgumentException("Invalid character, only alphanumeric characters are allowed: " + c);
            }
            int newIdx = (idx + offset) % 62;
            mainSb.append(POOL.charAt(newIdx));
        }
        String headNoise = StringUtils.getUniqueId(hLen);
        String tailNoise = StringUtils.getUniqueId(tLen);
        // 3位特征串：hLen offset tLen
        String feature = String.format("%d%d%d", hLen, offset, tLen);
        return feature + headNoise + mainSb + tailNoise;
    }

    /**
     * 解码还原原始字符串
     *
     * @param disturbStr 干扰后的目标字符串
     * @return 原始字符串
     */
    public static String decode(String disturbStr) {
        if (disturbStr == null || disturbStr.length() < FEATURE_LENGTH) {
            throw new IllegalArgumentException("String length must be at least 3");
        }
        // 读取3位特征
        String feature = disturbStr.substring(0, FEATURE_LENGTH);
        int hLen = feature.charAt(0) - '0';
        int offset = feature.charAt(1) - '0';
        int tLen = feature.charAt(2) - '0';
        String content = disturbStr.substring(FEATURE_LENGTH);
        // 截取主体偏移串：去掉头部噪声、尾部噪声
        int mainStart = hLen;
        int mainEnd = content.length() - tLen;
        if (mainStart >= mainEnd) {
            throw new IllegalArgumentException("Main content cannot be empty");
        }
        String mainStr = content.substring(mainStart, mainEnd);
        StringBuilder originSb = new StringBuilder();
        for (char c : mainStr.toCharArray()) {
            int idx = CHAR_TO_IDX.get(c);
            int originIdx = (idx - offset + 62) % 62;
            originSb.append(POOL.charAt(originIdx));
        }
        return originSb.toString();
    }


}
