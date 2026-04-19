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

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;

/**
 * 文本转拼音工具类
 *
 * @author Bigbird
 */
public class PinyinUtils {

    /**
     * 将中文转为无声调拼音，空格分隔
     *
     * @param chinese 中文文本
     * @return 无声调拼音
     */
    public static String toPinyin(String chinese) {
        chinese = SymbolToChineseUtils.replaceSymbolToChinese(chinese);
        return convert(chinese, false);
    }

    /**
     * 将中文转为带声调拼音（标准拼音）
     *
     * @param chinese 中文文本
     * @return 带声调拼音
     */
    public static String toPinyinWithTone(String chinese) {
        chinese = SymbolToChineseUtils.replaceSymbolToChinese(chinese);
        return convert(chinese, true);
    }

    private static String convert(String chinese, boolean withTone) {
        if (StringUtils.isBlank(chinese)) {
            return "";
        }
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        // 小写
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
        if (withTone) {
            // 带声调
            format.setToneType(HanyuPinyinToneType.WITH_TONE_MARK);
            format.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
        } else {
            // 无声调
            format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        }
        StringBuilder sb = new StringBuilder();
        char[] chars = chinese.toCharArray();
        for (char c : chars) {
            // 非中文直接保留
            if (!Character.toString(c).matches("[\\u4e00-\\u9fa5]")) {
                sb.append(c);
                continue;
            }
            try {
                // 取第一个读音（最常用）
                String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c, format);
                if (pinyins != null && pinyins.length > 0) {
                    sb.append(pinyins[0]).append(CommonConstants.SPACE);
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                sb.append(c);
            }
        }
        return sb.toString().trim();
    }

}
