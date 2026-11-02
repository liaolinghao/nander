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
package wang.bigbird.domain.framework.common.similarity.service.base.impl;

import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.common.similarity.base.enums.SentenceSimilarityAlgorithmEnum;
import wang.bigbird.domain.framework.common.similarity.exception.SimilarityException;
import wang.bigbird.domain.framework.common.similarity.service.base.ISentenceSimilarityService;
import wang.bigbird.domain.framework.common.similarity.support.strategy.sentence.ISentenceSimilarityStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 语句相似度服务
 *
 * @author Bigbird
 */
@Slf4j
public class SentenceSimilarityServiceImpl implements ISentenceSimilarityService {

    /**
     * 策略映射表
     */
    private final Map<SentenceSimilarityAlgorithmEnum, ISentenceSimilarityStrategy> strategies;

    public SentenceSimilarityServiceImpl(List<ISentenceSimilarityStrategy> strategyList) {
        this.strategies = new HashMap<>();
        for (ISentenceSimilarityStrategy strategy : strategyList) {
            this.strategies.put(strategy.getAlgorithm(), strategy);
        }
        log.info("SentenceSimilarity service initialized, {} algorithms registered", strategies.size());
    }

    @Override
    public double calculate(String targetSentence, String candidateSentence, SentenceSimilarityAlgorithmEnum algorithm) {
        if (targetSentence == null || candidateSentence == null) {
            throw new SimilarityException("Sentences cannot be null");
        }
        if (targetSentence.equals(candidateSentence)) {
            return 1.0;
        }
        if (targetSentence.isEmpty() || candidateSentence.isEmpty()) {
            return 0.0;
        }
        if (algorithm == null) {
            throw new SimilarityException("Algorithm strategy not set");
        }
        ISentenceSimilarityStrategy strategy = strategies.get(algorithm);
        if (strategy == null) {
            throw new SimilarityException("Algorithm strategy not found: " + algorithm);
        }
        return strategy.calculate(targetSentence, candidateSentence);
    }

}
