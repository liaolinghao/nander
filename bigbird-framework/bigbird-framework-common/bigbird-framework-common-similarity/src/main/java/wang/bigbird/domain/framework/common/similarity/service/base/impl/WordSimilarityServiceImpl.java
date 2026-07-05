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
import wang.bigbird.domain.framework.common.similarity.base.enums.WordSimilarityAlgorithmEnum;
import wang.bigbird.domain.framework.common.similarity.domain.bo.WordSimilarityBO;
import wang.bigbird.domain.framework.common.similarity.exception.SimilarityException;
import wang.bigbird.domain.framework.common.similarity.service.base.IWordSimilarityService;
import wang.bigbird.domain.framework.common.similarity.support.strategy.word.ISimilarityStrategy;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.SortUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 词语相似度服务
 *
 * @author Bigbird
 */
@Slf4j
public class WordSimilarityServiceImpl implements IWordSimilarityService {

    /**
     * 策略映射表
     */
    private final Map<WordSimilarityAlgorithmEnum, ISimilarityStrategy> strategies;

    public WordSimilarityServiceImpl(List<ISimilarityStrategy> strategyList) {
        this.strategies = new HashMap<>();
        for (ISimilarityStrategy strategy : strategyList) {
            this.strategies.put(strategy.getAlgorithm(), strategy);
        }
        log.info("WordSimilarity service initialized, {} algorithms registered", strategies.size());
    }

    @Override
    public double calculate(String targetWord, String candidateWord, WordSimilarityAlgorithmEnum algorithm) {
        if (targetWord == null || candidateWord == null) {
            throw new SimilarityException("Words cannot be null");
        }
        if (targetWord.equals(candidateWord)) {
            return 1.0;
        }
        if (targetWord.isEmpty() || candidateWord.isEmpty()) {
            return 0.0;
        }
        if (algorithm == null) {
            throw new SimilarityException("Algorithm strategy not set");
        }
        ISimilarityStrategy strategy = strategies.get(algorithm);
        if (strategy == null) {
            throw new SimilarityException("Algorithm strategy not found: " + algorithm);
        }
        return strategy.calculate(targetWord, candidateWord);
    }

    @Override
    public List<WordSimilarityBO> batchCalculate(String targetWord, List<String> candidateWords, WordSimilarityAlgorithmEnum algorithm) {
        if (targetWord == null || CollectionUtils.isEmpty(candidateWords)) {
            throw new SimilarityException("Words cannot be null");
        }
        ISimilarityStrategy strategy = strategies.get(algorithm);
        if (strategy == null) {
            throw new SimilarityException("Algorithm strategy not found: " + algorithm);
        }
        return candidateWords.stream().map(candidateWord -> {
            WordSimilarityBO wordSimilarityBO = new WordSimilarityBO();
            wordSimilarityBO.setTargetWord(targetWord);
            wordSimilarityBO.setCandidateWord(candidateWord);
            wordSimilarityBO.setAlgorithm(algorithm);
            wordSimilarityBO.setSimilarity(calculate(targetWord, candidateWord, algorithm));
            return wordSimilarityBO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<WordSimilarityBO> findMostSimilar(String targetWord, List<String> candidateWords, int topN, WordSimilarityAlgorithmEnum algorithm) {
        List<WordSimilarityBO> results = batchCalculate(targetWord, candidateWords, algorithm);
        SortUtils.sortByField(results, "similarity", true);
        return results.stream()
                .limit(topN)
                .collect(Collectors.toList());
    }

}
