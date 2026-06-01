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
package wang.bigbird.domain.framework.common.similarity.support.strategy.word.pinyin;

import wang.bigbird.domain.framework.common.similarity.base.enums.WordSimilarityAlgorithmEnum;
import wang.bigbird.domain.framework.common.similarity.support.calculator.word.pinyin.PinyinSimilarity;
import wang.bigbird.domain.framework.common.similarity.support.strategy.word.ISimilarityStrategy;

/**
 * 拼音相似度策略
 * <p>
 * 基于拼音匹配计算词语相似度
 * 适用于拼音相同的词语比较
 * </p>
 *
 * @author Bigbird
 */
public class PinyinSimilarityStrategy implements ISimilarityStrategy {

    @Override
    public WordSimilarityAlgorithmEnum getAlgorithm() {
        return WordSimilarityAlgorithmEnum.PINYIN;
    }

    @Override
    public double calculate(String targetWord, String candidateWord) {
        return PinyinSimilarity.getInstance().calculate(targetWord, candidateWord);
    }

}
