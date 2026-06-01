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
package wang.bigbird.domain.framework.common.similarity.service.base;

import wang.bigbird.domain.framework.common.similarity.base.enums.WordSimilarityAlgorithmEnum;
import wang.bigbird.domain.framework.common.similarity.domain.bo.WordSimilarityBO;

import java.util.List;

/**
 * 词语相似度服务
 *
 * @author Bigbird
 */
public interface IWordSimilarityService {

    /**
     * 使用指定算法计算词语相似度
     *
     * @param targetWord    目标词
     * @param candidateWord 候选词
     * @param algorithm     算法类型（CILIN, PINYIN, CONCEPT, CHAR_BASED）
     * @return 相似度值（0.0 - 1.0）
     */
    double calculate(String targetWord, String candidateWord, WordSimilarityAlgorithmEnum algorithm);

    /**
     * 使用指定算法批量计算词语相似度
     *
     * @param targetWord     目标词
     * @param candidateWords 候选词列表
     * @param algorithm      算法类型
     * @return 相似度结果列表
     */
    List<WordSimilarityBO> batchCalculate(String targetWord, List<String> candidateWords, WordSimilarityAlgorithmEnum algorithm);

    /**
     * 使用指定算法查找最相似的词语
     *
     * @param targetWord     目标词
     * @param candidateWords 候选词列表
     * @param topN           返回数量
     * @param algorithm      算法类型
     * @return 最相似的 Top N 个结果（按相似度降序）
     */
    List<WordSimilarityBO> findMostSimilar(String targetWord, List<String> candidateWords, int topN, WordSimilarityAlgorithmEnum algorithm);

}
