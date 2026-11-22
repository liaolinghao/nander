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
package wang.bigbird.domain.framework.common.similarity.base.tool;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 段落切句器（保留标点，中英文混合）。
 * <p>
 * 分层：按行 → 去列表标记 → 句末标点切分（含引号/括号闭合判断）→ 超长兜底。
 *
 * @author Bigbird
 */
@Slf4j
public final class ParagraphSplitter {

    /**
     * 二级兜底切分长度上限（UTF-16 单元；硬切时会回退到 code point 边界）
     */
    private static final int MAX_SEGMENT_LEN = 200;

    private static final Pattern LINE_BREAK = Pattern.compile("[\\r\\n]+");

    /**
     * 行首列表标记：1. / 1、 / 1) / (1) / - / • / *。
     * 末尾 lookahead (?=\\s|\\D|$) 防止把 "3.14" 这种小数误当序号。
     */
    private static final Pattern LIST_MARKER = Pattern.compile(
            "^\\s*(?:(?:\\(\\d{1,3}\\)|\\d{1,3}[.、)])(?=\\s|\\D|$)|[-•*]\\s+)");

    /**
     * 二级切分（仅超长兜底）：句内停顿；分号降级到这里，不再作硬句末
     */
    private static final Pattern SECONDARY = Pattern.compile("[，,、：:；;\\s]+");

    /**
     * 英文缩写白名单（含内部点的写法，取词逻辑已修正可命中）
     */
    private static final Set<String> ABBREVIATIONS = new HashSet<>(Arrays.asList(
            "mr", "mrs", "ms", "dr", "prof", "sr", "jr", "st", "vs", "etc",
            "e.g", "i.e", "u.s", "u.k", "a.m", "p.m", "no", "fig", "al", "eg", "ie"));

    private static final String OPEN_BRACKETS = "（(【[〔{《「『";
    private static final String CLOSE_BRACKETS = "）)】]〕}》」』";
    private static final String OPEN_QUOTES = "“‘";
    private static final String CLOSE_QUOTES = "”’";
    /**
     * 用于合并相邻句末标点
     */
    private static final String END_PUNCT = "。！？!?….";

    private ParagraphSplitter() {
    }

    public static List<String> split(String paragraph) {
        if (paragraph == null || paragraph.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (String rawLine : LINE_BREAK.split(paragraph.trim())) {
            String line = stripListMarker(rawLine.trim());
            if (line.isEmpty()) {
                continue;
            }
            for (String sentence : splitSentences(line)) {
                addWithFallback(result, sentence);
            }
        }
        return result;
    }

    // ===================== 一级：句末切分（保留标点） =====================

    private static List<String> splitSentences(String text) {
        int n = text.length();
        int[] depthAfter = computeDepths(text);
        List<String> out = new ArrayList<>();
        int start = 0;
        int i = 0;
        while (i < n) {
            char c = text.charAt(i);
            if (!isEndCandidate(c, text, i)) {
                i++;
                continue;
            }
            int before = (i == 0) ? 0 : depthAfter[i - 1];
            boolean boundary;
            if (before == 0) {
                // 不在成对符号内，直接可切
                boundary = true;
            } else {
                // 在引号/括号内：仅当紧跟闭合符号、且闭合后深度归零，才算句末
                int j = i + 1;
                boundary = j < n && isClosing(text.charAt(j)) && depthAfter[j] == 0;
            }
            if (!boundary) {
                i++;
                continue;
            }
            // 吞并紧随其后的连续句末标点与闭合符号（。！” 等整体归前句）
            int end = i + 1;
            while (end < n) {
                char nc = text.charAt(end);
                if (END_PUNCT.indexOf(nc) >= 0 || isClosing(nc)) {
                    end++;
                } else {
                    break;
                }
            }
            String sentence = text.substring(start, end).trim();
            if (!sentence.isEmpty()) {
                out.add(sentence);
            }
            start = end;
            i = end;
        }
        if (start < n) {
            String tail = text.substring(start).trim();
            if (!tail.isEmpty()) {
                out.add(tail);
            }
        }
        return out;
    }

    /**
     * 预计算每个位置“处理完该字符后”的成对符号深度。
     * 深度只由开/闭括号与引号决定，与切分跳转无关，后续判断稳定可靠。
     */
    private static int[] computeDepths(String s) {
        int n = s.length();
        int[] depthAfter = new int[n];
        int d = 0;
        boolean inDquote = false;
        for (int k = 0; k < n; k++) {
            char c = s.charAt(k);
            if (OPEN_BRACKETS.indexOf(c) >= 0 || OPEN_QUOTES.indexOf(c) >= 0) {
                d++;
            } else if (CLOSE_BRACKETS.indexOf(c) >= 0 || CLOSE_QUOTES.indexOf(c) >= 0) {
                if (d > 0) {
                    d--;
                }
            } else if (c == '"') {
                // 英文直引号同字符，用奇偶切换
                if (inDquote) {
                    if (d > 0) {
                        d--;
                    }
                    inDquote = false;
                } else {
                    d++;
                    inDquote = true;
                }
            }
            depthAfter[k] = d;
        }
        return depthAfter;
    }

    /**
     * 判断某字符是否可能构成一级句末边界
     */
    private static boolean isEndCandidate(char c, String text, int i) {
        if (c == '。' || c == '！' || c == '？' || c == '!' || c == '?' || c == '…') {
            return true;
        }
        if (c == '.') {
            if (isProtectedDot(text, i)) {
                // 缩写/小数/编号内的点
                return false;
            }
            if (i + 1 < text.length()) {
                char nx = text.charAt(i + 1);
                if (!Character.isWhitespace(nx) && !isClosing(nx)) {
                    // 点后非空白/非闭合，不是句末
                    return false;
                }
                // 数字 + 点 + 空白 + 中文：视为“第3. 条”类编号，不切
                char prev = i > 0 ? text.charAt(i - 1) : 0;
                if (Character.isDigit(prev) && Character.isWhitespace(nx)) {
                    int k = i + 1;
                    while (k < text.length() && Character.isWhitespace(text.charAt(k))) {
                        k++;
                    }
                    if (k < text.length() && isChinese(text.charAt(k))) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 判断英文句点是否受保护（不切）。
     * 已由“点后必须是空白/闭合”排除小数与 URL，这里处理：
     * 1. 白名单缩写（取词支持内部点，e.g./U.S. 可命中）；
     * 2. 缩写链（前前字符是 '.'，如 U.S.A. 的末尾点）。
     */
    private static boolean isProtectedDot(String text, int dotIndex) {
        int prevIdx = dotIndex - 1;
        if (prevIdx < 0) {
            return false;
        }
        char prev = text.charAt(prevIdx);

        int start = prevIdx;
        while (start >= 0
                && (Character.isLetter(text.charAt(start)) || text.charAt(start) == '.')) {
            start--;
        }
        String word = text.substring(start + 1, dotIndex).toLowerCase();
        if (!word.isEmpty() && ABBREVIATIONS.contains(word)) {
            return true;
        }

        if (Character.isUpperCase(prev)) {
            char prevPrev = dotIndex >= 2 ? text.charAt(dotIndex - 2) : 0;
            if (prevPrev == '.') {
                // U.S.A. 之类缩写链
                return true;
            }
        }
        return false;
    }

    private static boolean isClosing(char c) {
        return CLOSE_BRACKETS.indexOf(c) >= 0 || CLOSE_QUOTES.indexOf(c) >= 0 || c == '"';
    }

    private static boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FFF;
    }

    // ===================== 行首列表标记 =====================

    private static String stripListMarker(String line) {
        Matcher m = LIST_MARKER.matcher(line);
        if (m.find()) {
            return line.substring(m.end()).trim();
        }
        return line;
    }

    // ===================== 二级：超长兜底（迭代，含空串与代理对保护） =====================

    private static void addWithFallback(List<String> out, String segment) {
        String rest = segment;
        while (rest != null && !rest.isEmpty()) {
            if (rest.length() <= MAX_SEGMENT_LEN) {
                String tail = rest.trim();
                if (!tail.isEmpty()) {
                    // 防空串混入
                    out.add(tail);
                }
                return;
            }
            int cut = findSecondaryCut(rest);
            if (cut <= 0) {
                cut = codePointSafeIndex(rest, MAX_SEGMENT_LEN);
            }
            String head = rest.substring(0, cut).trim();
            if (!head.isEmpty()) {
                out.add(head);
            }
            rest = rest.substring(cut).trim();
        }
    }

    private static int findSecondaryCut(String segment) {
        Matcher m = SECONDARY.matcher(segment);
        int lastCut = -1;
        while (m.find()) {
            if (m.end() <= MAX_SEGMENT_LEN) {
                lastCut = m.end();
            } else {
                break;
            }
        }
        return lastCut;
    }

    /**
     * 返回不超过 limit 且不切断 UTF-16 代理对的切点
     */
    private static int codePointSafeIndex(String s, int limit) {
        int idx = Math.min(limit, s.length());
        if (idx > 0 && idx < s.length() && Character.isLowSurrogate(s.charAt(idx))) {
            // 切点落在代理对中间，回退一个单元
            idx--;
        }
        return idx;
    }

}

