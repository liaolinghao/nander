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
package wang.bigbird.domain.framework.common.similarity.base.util;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import java.util.*;

/**
 * 相似度计算辅助通用工具
 *
 * @author Bigbird
 */
public class Utils {

    /**
     * 停用词
     */
    private static final Set<String> STOP_WORDS;
    /**
     * 业务白名单
     */
    private static final Set<String> WHITE_WORDS;

    static {
        // 加载resources/stopword.txt，失败则加载内置兜底停用词
        STOP_WORDS = loadStopWordFile("/stop.txt");
        // IT行业单字白名单，不会被过滤
        WHITE_WORDS = loadWhiteWordFile("/white.txt");
    }

    private static Set<String> loadWhiteWordFile(String s) {
        return null;
    }

    private static Set<String> loadStopWordFile(String s) {
        return null;
    }

    /**
     * HanLP分词+清洗过滤
     *
     * @param text 输入文本
     * @return 有效关键词列表
     */
    public static List<String> segClean(String text) {
        List<String> res = new ArrayList<>();
        if (StringUtils.isBlank(text)) {
            return res;
        }
        List<Term> termList = HanLP.segment(text);
        for (Term term : termList) {
            String word = term.word.trim();
            String nature = term.nature.toString();
            // 过滤规则：标点w、数字m、停用词、无效单字（白名单放行）
            if (word.isBlank()) {
                continue;
            }
            if ("w".equals(nature) || "m".equals(nature)) {
                continue;
            }
            if (WHITE_WORDS.contains(word)) {
                res.add(word);
                continue;
            }
            if (word.length() <= 1) {
                continue;
            }
            if (STOP_WORDS.contains(word)) {
                continue;
            }
            res.add(word);
        }
        return res;
    }

    /**
     * 文本清洗：过滤非中英数字字符、统一小写
     *
     * @param text 原始文本
     * @return 清洗后纯小写中英数字字符串
     */
    public static String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[^a-zA-Z0-9\u4e00-\u9fa5]", "")
                .toLowerCase();
    }

    /**
     * 生成三元组集合（去重，用于Jaccard）
     *
     * @param text 原始文本
     * @return 不重复三元片段集合
     */
    public static Set<String> generateTrigramSet(String text) {
        Set<String> trigramSet = new HashSet<>();
        if (StringUtils.isBlank(text)) {
            return trigramSet;
        }
        // 标准填充：前后各补一个空格
        String padded = " " + text.trim() + " ";
        int len = padded.length();
        // 滑动窗口截取3字符
        for (int i = 0; i <= len - 3; i++) {
            String tri = padded.substring(i, i + 3);
            trigramSet.add(tri);
        }
        return trigramSet;
    }

    /**
     * 生成三元组计数Map（重复片段计数，用于余弦相似度）
     *
     * @param text 原始文本
     * @return 三元组计数Map
     */
    public static Map<String, Integer> generateTrigramMap(String text) {
        Map<String, Integer> map = new TreeMap<>();
        if (StringUtils.isBlank(text)) {
            return map;
        }
        String padded = " " + text.trim() + " ";
        int len = padded.length();
        for (int i = 0; i <= len - 3; i++) {
            String tri = padded.substring(i, i + 3);
            map.put(tri, map.getOrDefault(tri, 0) + 1);
        }
        return map;
    }

}
