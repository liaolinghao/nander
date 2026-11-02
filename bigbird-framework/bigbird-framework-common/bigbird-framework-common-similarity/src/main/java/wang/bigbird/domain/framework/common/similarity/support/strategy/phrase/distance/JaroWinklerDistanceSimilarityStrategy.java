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
package wang.bigbird.domain.framework.common.similarity.support.strategy.phrase.distance;

import wang.bigbird.domain.framework.common.similarity.base.enums.PhraseSimilarityAlgorithmEnum;
import wang.bigbird.domain.framework.common.similarity.support.calculator.phrase.distance.JaroWinklerDistanceSimilarity;
import wang.bigbird.domain.framework.common.similarity.support.strategy.phrase.IPhraseSimilarityStrategy;

/**
 * JaroWinkler 短字符串优化相似度策略
 * <p>
 * 一种专门为比较短字符串（如人名、地名）设计的相似度算法，对换位和前缀敏感。
 * </p>
 *
 * @author Bigbird
 */
public class JaroWinklerDistanceSimilarityStrategy implements IPhraseSimilarityStrategy {

    @Override
    public PhraseSimilarityAlgorithmEnum getAlgorithm() {
        return PhraseSimilarityAlgorithmEnum.JaroWinklerDistance;
    }

    @Override
    public double calculate(String targetPhrase, String candidatePhrase) {
        return JaroWinklerDistanceSimilarity.getInstance().calculate(targetPhrase, candidatePhrase);
    }

}
