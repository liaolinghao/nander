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
package wang.bigbird.domain.framework.common.similarity.support.strategy.phrase.trigram;

import wang.bigbird.domain.framework.common.similarity.base.enums.PhraseSimilarityAlgorithmEnum;
import wang.bigbird.domain.framework.common.similarity.support.calculator.phrase.trigram.TrigramJaccardSimilarity;
import wang.bigbird.domain.framework.common.similarity.support.strategy.phrase.IPhraseSimilarityStrategy;

/**
 * Trigram 杰卡德相似度策略
 * <p>
 * 把字符串切成若干个连续3个字符的小片段（Trigram），然后用杰卡德系数来衡量两个字符串的相似程度，
 * 它的核心思想是：如果两个字符串包含大量相同的连续3字符片段，那它们大概率是相似的。
 *
 * </p>
 *
 * @author Bigbird
 */
public class TrigramJaccardSimilarityStrategy implements IPhraseSimilarityStrategy {

    @Override
    public PhraseSimilarityAlgorithmEnum getAlgorithm() {
        return PhraseSimilarityAlgorithmEnum.TrigramJaccard;
    }

    @Override
    public double calculate(String targetPhrase, String candidatePhrase) {
        return TrigramJaccardSimilarity.getInstance().calculate(targetPhrase, candidatePhrase);
    }

}
