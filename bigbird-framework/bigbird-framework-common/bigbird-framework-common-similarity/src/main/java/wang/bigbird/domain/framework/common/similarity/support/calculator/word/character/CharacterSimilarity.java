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

import wang.bigbird.domain.framework.common.similarity.support.calculator.word.AbstractWordSimilarity;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于字符的相似度计算
 * <p>
 * 算法：
 * 相似度 = 0.6 × 字符重叠度
 * + 0.4 × 长度系数 × 位置权重平均分
 *
 * @author Bigbird
 */
public class CharacterSimilarity extends AbstractWordSimilarity {

    private final double alpha = 0.6;

    private final double beta = 0.4;

    private static CharacterSimilarity instance = null;

    public static CharacterSimilarity getInstance() {
        if (instance == null) {
            instance = new CharacterSimilarity();
        }
        return instance;
    }

    private CharacterSimilarity() {
    }

    @Override
    protected double doCalculate(String target, String candidate) {
        List<Character> sameChars = new ArrayList<>();
        String longString = target.length() >= candidate.length() ? target : candidate;
        String shortString = target.length() < candidate.length() ? target : candidate;
        for (int i = 0; i < longString.length(); i++) {
            Character ch = longString.charAt(i);
            if (shortString.contains(ch.toString())) {
                sameChars.add(ch);
            }
        }
        double dp = Math.min(1.0 * target.length() / candidate.length(), 1.0 * candidate.length() / target.length());
        double part1 = alpha * (1.0 * sameChars.size() / target.length() + 1.0 * sameChars.size() / candidate.length()) / 2.0;
        double part2 = beta * dp * (getWeightedResult(target, sameChars) + getWeightedResult(candidate, sameChars)) / 2.0;
        return part1 + part2;
    }

    /**
     * 位置权重
     * 相同字符出现在越后面 → 得分越高
     *
     * @param word
     * @param sameChars
     * @return
     */
    private double getWeightedResult(String word, List<Character> sameChars) {
        double top = 0.0;
        double bottom = 0.0;
        for (int i = 0; i < word.length(); i++) {
            if (sameChars.contains(word.charAt(i))) {
                top += (i + 1);
            }
            bottom += (i + 1);
        }
        return 1.0 * top / bottom;
    }

}
