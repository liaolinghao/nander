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
package wang.bigbird.domain.framework.common.similarity.support.strategy.word.cilin;

import wang.bigbird.domain.framework.common.similarity.base.enums.WordSimilarityAlgorithmEnum;
import wang.bigbird.domain.framework.common.similarity.support.calculator.word.cilin.CilinSimilarity;
import wang.bigbird.domain.framework.common.similarity.support.strategy.word.ISimilarityStrategy;

/**
 * 词林相似度策略
 * <p>
 * 基于词林语义词典计算词语相似度
 * 适用于语义相关的词语比较
 * </p>
 *
 * @author Bigbird
 */
public class CilinSimilarityStrategy implements ISimilarityStrategy {

    @Override
    public WordSimilarityAlgorithmEnum getAlgorithm() {
        return WordSimilarityAlgorithmEnum.CILIN;
    }

    @Override
    public double calculate(String targetWord, String candidateWord) {
        return CilinSimilarity.getInstance().calculate(targetWord, candidateWord);
    }

}
