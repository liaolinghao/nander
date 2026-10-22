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
package wang.bigbird.domain.framework.common.similarity.support.calculator.word.character;

import com.google.common.collect.Maps;
import wang.bigbird.domain.framework.common.similarity.support.calculator.word.AbstractWordSimilarity;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于字符的相似度计算
 * <p>
 * 算法：
 * 相似度 = 0.6 × 字符重叠度
 * + 0.4 × 长度系数 × 位置权重平均分
 * <p>
 * 设计要点：
 * <ol>
 *   <li>以字符频次（Multiset）计算重叠字符，避免重复字符被重复计数导致分数虚高；</li>
 *   <li>目标词与候选词的处理完全对称，保证 sim(a,b) = sim(b,a)；</li>
 *   <li>单例使用静态内部类实现，兼顾懒加载与线程安全。</li>
 * </ol>
 *
 * @author Bigbird
 */
public class CharacterSimilarity extends AbstractWordSimilarity {

    /**
     * 字符重叠度权重
     */
    private static final double ALPHA = 0.6;

    /**
     * 位置权重部分权重
     */
    private static final double BETA = 0.4;

    private CharacterSimilarity() {
    }

    private static class Holder {
        private static final CharacterSimilarity INSTANCE = new CharacterSimilarity();
    }

    public static CharacterSimilarity getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    protected double doCalculate(String target, String candidate) {
        // 1. 统计两个词的字符频次
        Map<Character, Integer> freqTarget = countChars(target);
        Map<Character, Integer> freqCandidate = countChars(candidate);
        // 2. 计算重叠字符频次（每字符取两边较小值，天然对称）
        Map<Character, Integer> overlapFreq = new HashMap<>();
        int overlapCount = 0;
        for (Map.Entry<Character, Integer> entry : freqTarget.entrySet()) {
            Integer other = freqCandidate.get(entry.getKey());
            if (other != null && other > 0) {
                int min = Math.min(entry.getValue(), other);
                overlapFreq.put(entry.getKey(), min);
                overlapCount += min;
            }
        }
        // 3. 字符重叠度（对称）
        double overlapRatio = 0.5 * ((double) overlapCount / target.length()
                + (double) overlapCount / candidate.length());
        double part1 = ALPHA * overlapRatio;
        // 4. 长度系数（对称）
        double lengthFactor = Math.min(
                (double) target.length() / candidate.length(),
                (double) candidate.length() / target.length());
        // 5. 位置权重平均分（对称）
        double positionAvg = (getWeightedPositionScore(target, overlapFreq)
                + getWeightedPositionScore(candidate, overlapFreq)) / 2.0;
        double part2 = BETA * lengthFactor * positionAvg;
        return part1 + part2;
    }

    /**
     * 统计待计算的词语中每个字符出现的次数
     *
     * @param word 待计算的词语
     * @return 字符频数
     */
    private Map<Character, Integer> countChars(String word) {
        Map<Character, Integer> freq = Maps.newHashMapWithExpectedSize(word.length());
        for (int i = 0; i < word.length(); i++) {
            freq.merge(word.charAt(i), 1, Integer::sum);
        }
        return freq;
    }

    /**
     * 位置权重
     * 相同字符出现在越后面 → 得分越高
     * <p>
     * 按字符频次逐次消耗，避免同一字符被重复匹配。
     *
     * @param word        待计算的词语
     * @param overlapFreq 重叠字符及允许匹配的最大次数
     * @return 位置加权得分，范围 [0,1]
     */
    private double getWeightedPositionScore(String word, Map<Character, Integer> overlapFreq) {
        Map<Character, Integer> remaining = new HashMap<>(overlapFreq);
        double top = 0.0;
        double bottom = 0.0;
        for (int i = 0; i < word.length(); i++) {
            double weight = i + 1;
            bottom += weight;
            char ch = word.charAt(i);
            Integer left = remaining.get(ch);
            if (left != null && left > 0) {
                top += weight;
                remaining.put(ch, left - 1);
            }
        }
        return bottom == 0.0 ? 0.0 : top / bottom;
    }

}
