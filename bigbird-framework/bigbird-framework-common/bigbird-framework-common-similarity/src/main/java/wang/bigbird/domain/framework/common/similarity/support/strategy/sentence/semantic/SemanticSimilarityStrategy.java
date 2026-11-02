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
package wang.bigbird.domain.framework.common.similarity.support.strategy.sentence.semantic;

import wang.bigbird.domain.framework.common.similarity.base.enums.SentenceSimilarityAlgorithmEnum;
import wang.bigbird.domain.framework.common.similarity.support.calculator.sentence.semantic.SemanticSimilarity;
import wang.bigbird.domain.framework.common.similarity.support.strategy.sentence.ISentenceSimilarityStrategy;

/**
 * 语义相似度策略
 * <p>
 * 基于语义计算相似度，使用知网（HowNet）概念相似度判断是否属于相同词
 * </p>
 *
 * @author Bigbird
 */
public class SemanticSimilarityStrategy implements ISentenceSimilarityStrategy {

    @Override
    public SentenceSimilarityAlgorithmEnum getAlgorithm() {
        return SentenceSimilarityAlgorithmEnum.Semantic;
    }

    @Override
    public double calculate(String targetSentence, String candidateSentence) {
        return SemanticSimilarity.getInstance().calculate(targetSentence, candidateSentence);
    }

}
