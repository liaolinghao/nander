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
import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.common.similarity.config.property.SimilarityProperties;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.tool.Coder;
import wang.bigbird.domain.framework.core.base.util.FileUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.core.base.util.event.TraverseEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 相似度计算辅助通用工具
 *
 * @author Bigbird
 */
@Slf4j
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
        STOP_WORDS = loadWordFile(SimilarityProperties.StopWordPath);
        // IT行业单字白名单，不会被过滤
        WHITE_WORDS = loadWordFile(SimilarityProperties.WhiteWordPath);
    }

    private static Set<String> loadWordFile(String wordPath) {
        Set<String> set = new HashSet<>();
        try (InputStream inputStream = Utils.class.getClassLoader().getResourceAsStream(wordPath)) {
            // 解析词库
            TraverseEvent<String> event = line -> {
                if (StringUtils.isNotBlank(line)) {
                    set.add(line.trim());
                }
                return true;
            };
            FileUtils.traverseLines(inputStream, Coder.DEFAULT_ENCODING, event);
        } catch (IOException e) {
            log.error("LoadWordFile:", e);
        }
        return set;
    }

    /**
     * HanLP分词+清洗过滤
     *
     * @param text 输入文本
     * @return 有效关键词列表
     */
    public static List<String> segClean(String text) {
        if (StringUtils.isBlank(text)) {
            return Collections.EMPTY_LIST;
        }
        List<String> res = new ArrayList<>();
        List<Term> termList = HanLP.segment(text);
        for (Term term : termList) {
            String word = term.word.trim();
            String nature = term.nature != null ? term.nature.toString() : "";
            // 过滤规则：标点w、数字m、停用词、无效单字（白名单放行）
            if (word.isBlank()) {
                continue;
            }
            if (nature.startsWith("w") || nature.startsWith("m")) {
                continue;
            }
            if (WHITE_WORDS.contains(word)) {
                res.add(word.toLowerCase());
                continue;
            }
            if (word.length() <= 1) {
                continue;
            }
            if (STOP_WORDS.contains(word)) {
                continue;
            }
            res.add(word.toLowerCase());
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
            return CommonConstants.EMPTY;
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
        if (StringUtils.isBlank(text)) {
            return Collections.EMPTY_SET;
        }
        Set<String> trigramSet = new HashSet<>();
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
     * 通用向量余弦计算：适用于TF-IDF向量、Embedding句向量
     *
     * @param vecA 向量A
     * @param vecB 向量B
     * @return 余弦相似度
     */
    public static double cosineSimilarity(double[] vecA, double[] vecB) {
        if (vecA == null || vecB == null || vecA.length != vecB.length) {
            return 0.0;
        }
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vecA.length; i++) {
            dot += vecA[i] * vecB[i];
            normA += vecA[i] * vecA[i];
            normB += vecB[i] * vecB[i];
        }
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

}
