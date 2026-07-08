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
import wang.bigbird.domain.framework.common.similarity.base.enums.PhraseSimilarityAlgorithmEnum;
import wang.bigbird.domain.framework.common.similarity.exception.SimilarityException;
import wang.bigbird.domain.framework.common.similarity.service.base.IPhraseSimilarityService;
import wang.bigbird.domain.framework.common.similarity.support.strategy.phrase.ISimilarityStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短语相似度服务
 *
 * @author Bigbird
 */
@Slf4j
public class PhraseSimilarityServiceImpl implements IPhraseSimilarityService {

    /**
     * 策略映射表
     */
    private final Map<PhraseSimilarityAlgorithmEnum, ISimilarityStrategy> strategies;

    public PhraseSimilarityServiceImpl(List<ISimilarityStrategy> strategyList) {
        this.strategies = new HashMap<>();
        for (ISimilarityStrategy strategy : strategyList) {
            this.strategies.put(strategy.getAlgorithm(), strategy);
        }
        log.info("PhraseSimilarity service initialized, {} algorithms registered", strategies.size());
    }

    @Override
    public double calculate(String targetPhrase, String candidatePhrase, PhraseSimilarityAlgorithmEnum algorithm) {
        if (targetPhrase == null || candidatePhrase == null) {
            throw new SimilarityException("Phrases cannot be null");
        }
        if (targetPhrase.equals(candidatePhrase)) {
            return 1.0;
        }
        if (targetPhrase.isEmpty() || candidatePhrase.isEmpty()) {
            return 0.0;
        }
        if (algorithm == null) {
            throw new SimilarityException("Algorithm strategy not set");
        }
        ISimilarityStrategy strategy = strategies.get(algorithm);
        if (strategy == null) {
            throw new SimilarityException("Algorithm strategy not found: " + algorithm);
        }
        return strategy.calculate(targetPhrase, candidatePhrase);
    }

}
