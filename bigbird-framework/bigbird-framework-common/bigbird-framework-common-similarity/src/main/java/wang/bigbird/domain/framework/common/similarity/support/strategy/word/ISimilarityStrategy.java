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
package wang.bigbird.domain.framework.common.similarity.support.strategy.word;

import wang.bigbird.domain.framework.common.similarity.base.enums.WordSimilarityAlgorithmEnum;

/**
 * 词语相似度计算策略
 *
 * @author Bigbird
 */
public interface ISimilarityStrategy {

    /**
     * 获取策略对应的算法类型
     *
     * @return 算法类型枚举
     */
    WordSimilarityAlgorithmEnum getAlgorithm();

    /**
     * 计算两个词语的相似度
     *
     * @param targetWord    目标词
     * @param candidateWord 候选词
     * @return 相似度值（0.0 - 1.0）
     */
    double calculate(String targetWord, String candidateWord);

}
