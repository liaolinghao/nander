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
package wang.bigbird.domain.framework.common.similarity.support.calculator.phrase.cos;

import wang.bigbird.domain.framework.common.similarity.base.util.Utils;
import wang.bigbird.domain.framework.common.similarity.support.calculator.phrase.AbstractPhraseSimilarity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于文本向量化后的向量夹角的相似度计算
 * 将短语分词并表示为词频、TF-IDF 或词嵌入向量，分别得到两个短语的特征向量，
 * 使用「两向量点积 / 两向量模长乘积」得到余弦相似度 [0,1]；
 * 兼顾整体语义方向特征，对词序变化不敏感
 *
 * @author Bigbird
 */
public class CosSimilarity extends AbstractPhraseSimilarity {

    private CosSimilarity() {
    }

    private static class Holder {
        private static final CosSimilarity INSTANCE = new CosSimilarity();
    }

    public static CosSimilarity getInstance() {
        return CosSimilarity.Holder.INSTANCE;
    }

    @Override
    protected double doCalculatePhrase(String target, String candidate) {
        List<String> words1 = Utils.segClean(target);
        List<String> words2 = Utils.segClean(candidate);
        Set<String> allWords = new HashSet<>();
        allWords.addAll(words1);
        allWords.addAll(words2);
        List<String> dict = new ArrayList<>(allWords);
        double[] v1 = new double[dict.size()];
        double[] v2 = new double[dict.size()];
        Map<String, Long> cnt1 = words1.stream().collect(Collectors.groupingBy(k -> k, Collectors.counting()));
        Map<String, Long> cnt2 = words2.stream().collect(Collectors.groupingBy(k -> k, Collectors.counting()));
        for (int i = 0; i < dict.size(); i++) {
            String w = dict.get(i);
            v1[i] = cnt1.getOrDefault(w, 0L);
            v2[i] = cnt2.getOrDefault(w, 0L);
        }
        return Utils.cosineSimilarity(v1, v2);
    }


}
