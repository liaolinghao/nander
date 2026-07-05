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
package wang.bigbird.domain.framework.common.similarity.support.strategy.phrase.cos;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import wang.bigbird.domain.framework.common.similarity.base.enums.PhraseSimilarityAlgorithmEnum;
import wang.bigbird.domain.framework.common.similarity.support.strategy.phrase.ISimilarityStrategy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 余弦相似度策略
 * <p>
 * 中文分词：把短语拆成独立词语（如Java分布式缓存 → Java、分布式、缓存）；
 * 清洗过滤：剔除停用词、标点、数字、无意义单字；
 * 构建全局词表：合并两段文本所有有效词汇，作为向量维度；
 * 生成词频向量：每个维度值 = 词语出现次数；
 * 套余弦公式计算相似度。
 *
 * </p>
 *
 * @author Bigbird
 */
public class CosSimilarityStrategy implements ISimilarityStrategy {



    @Override
    public PhraseSimilarityAlgorithmEnum getAlgorithm() {
        return PhraseSimilarityAlgorithmEnum.Cosine;
    }

    @Override
    public double calculate(String targetPhrase, String candidatePhrase) {
        return 0;
    }


}
