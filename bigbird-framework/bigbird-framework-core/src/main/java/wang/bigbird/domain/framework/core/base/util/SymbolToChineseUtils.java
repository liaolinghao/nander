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

import java.util.HashMap;
import java.util.Map;

/**
 * 特殊符号转中文工具
 * 例如 @ → 艾特、! → 感叹号 / 惊号、. → 点、# → 井号 等
 *
 * @author Bigbird
 */
public class SymbolToChineseUtils {

    private static final Map<String, String> SYMBOL_MAP = new HashMap<>();

    static {
        // 常用符号 → 中文
        SYMBOL_MAP.put("@", "艾特");
        SYMBOL_MAP.put("!", "感叹号");
        SYMBOL_MAP.put("#", "井号");
        SYMBOL_MAP.put("$", "美元符");
        SYMBOL_MAP.put("%", "百分号");
        SYMBOL_MAP.put("&", "与");
        SYMBOL_MAP.put("*", "星号");
        SYMBOL_MAP.put("(", "左括号");
        SYMBOL_MAP.put(")", "右括号");
        SYMBOL_MAP.put("-", "减号/横杠");
        SYMBOL_MAP.put("=", "等号");
        SYMBOL_MAP.put("+", "加号");
        SYMBOL_MAP.put(".", "点");
        SYMBOL_MAP.put(",", "逗号");
        SYMBOL_MAP.put(":", "冒号");
        SYMBOL_MAP.put(";", "分号");
        SYMBOL_MAP.put("?", "问号");
        SYMBOL_MAP.put("`", "反引号");
        SYMBOL_MAP.put("~", "波浪号");
        SYMBOL_MAP.put("<", "小于号");
        SYMBOL_MAP.put(">", "大于号");
        SYMBOL_MAP.put("/", "斜杠");
        SYMBOL_MAP.put("\\", "反斜杠");
        SYMBOL_MAP.put("|", "竖线");
        SYMBOL_MAP.put("{", "左大括号");
        SYMBOL_MAP.put("}", "右大括号");
        SYMBOL_MAP.put("[", "左中括号");
        SYMBOL_MAP.put("]", "右中括号");
        SYMBOL_MAP.put("\"", "双引号");
        SYMBOL_MAP.put("'", "单引号");
        SYMBOL_MAP.put("_", "下划线");
        SYMBOL_MAP.put("0", "零");
        SYMBOL_MAP.put("1", "一");
        SYMBOL_MAP.put("2", "二");
        SYMBOL_MAP.put("3", "三");
        SYMBOL_MAP.put("4", "四");
        SYMBOL_MAP.put("5", "五");
        SYMBOL_MAP.put("6", "六");
        SYMBOL_MAP.put("7", "七");
        SYMBOL_MAP.put("8", "八");
        SYMBOL_MAP.put("9", "九");
    }

    /**
     * 把字符串里的所有特殊符号 替换成 中文
     *
     * @param text 原始字符串
     * @return 转换后字符串
     */
    public static String replaceSymbolToChinese(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            String symbol = String.valueOf(c);
            // 如果是符号，替换成中文
            if (SYMBOL_MAP.containsKey(symbol)) {
                result.append(SYMBOL_MAP.get(symbol));
            } else {
                // 不是符号，保留原字符
                result.append(c);
            }
        }
        return result.toString();
    }

}
