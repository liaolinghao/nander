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
package wang.bigbird.domain.framework.common.similarity.config.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import wang.bigbird.domain.framework.common.similarity.service.base.IWordSimilarityService;
import wang.bigbird.domain.framework.common.similarity.service.base.impl.WordSimilarityServiceImpl;
import wang.bigbird.domain.framework.common.similarity.support.strategy.word.ISimilarityStrategy;
import wang.bigbird.domain.framework.common.similarity.support.strategy.word.character.CharacterSimilarityStrategy;
import wang.bigbird.domain.framework.common.similarity.support.strategy.word.cilin.CilinSimilarityStrategy;
import wang.bigbird.domain.framework.common.similarity.support.strategy.word.concept.ConceptSimilarityStrategy;
import wang.bigbird.domain.framework.common.similarity.support.strategy.word.pinyin.PinyinSimilarityStrategy;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * 相似度计算器配置
 *
 * @author Bigbird
 */
@Slf4j
@ComponentScan("wang.bigbird.domain.framework.common.similarity")
@Configuration
public class SimilarityConfiguration {

    @PostConstruct
    public void init() {
        log.info("Init similarity framework.");
    }

    /**
     * 词林相似度策略
     */
    @Bean
    @ConditionalOnMissingBean(CilinSimilarityStrategy.class)
    public CilinSimilarityStrategy cilinSimilarityStrategy() {
        return new CilinSimilarityStrategy();
    }

    /**
     * 拼音相似度策略
     */
    @Bean
    @ConditionalOnMissingBean(PinyinSimilarityStrategy.class)
    public PinyinSimilarityStrategy pinyinSimilarityStrategy() {
        return new PinyinSimilarityStrategy();
    }

    /**
     * 概念相似度策略
     */
    @Bean
    @ConditionalOnMissingBean(ConceptSimilarityStrategy.class)
    public ConceptSimilarityStrategy conceptSimilarityStrategy() {
        return new ConceptSimilarityStrategy();
    }

    /**
     * 字面相似度策略
     */
    @Bean
    @ConditionalOnMissingBean(CharacterSimilarityStrategy.class)
    public CharacterSimilarityStrategy characterSimilarityStrategy() {
        return new CharacterSimilarityStrategy();
    }

    /**
     * 注册相似度服务
     */
    @Bean
    @ConditionalOnMissingBean(IWordSimilarityService.class)
    public IWordSimilarityService wordSimilarityService(
            CilinSimilarityStrategy cilinStrategy,
            PinyinSimilarityStrategy pinyinStrategy,
            ConceptSimilarityStrategy conceptStrategy,
            CharacterSimilarityStrategy characterSimilarityStrategy) {
        List<ISimilarityStrategy> strategies = Arrays.asList(
                cilinStrategy,
                pinyinStrategy,
                conceptStrategy,
                characterSimilarityStrategy
        );
        return new WordSimilarityServiceImpl(strategies);
    }

}
