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
package wang.bigbird.domain.framework.common.similarity.support.calculator.sentence;

import wang.bigbird.domain.framework.common.similarity.base.util.Utils;
import wang.bigbird.domain.framework.common.similarity.support.calculator.AbstractSimilarity;

import java.util.List;

/**
 * 处理语句层面的相似度计算
 *
 * @author Bigbird
 */
public abstract class AbstractSentenceSimilarity extends AbstractSimilarity {


    @Override
    protected double doCalculate(String target, String candidate) {
        // 分词与清洗，后续所有计算都只在"承载语义的词"上进行
        List<String> targetWords = Utils.segClean(target);
        List<String> candidateWords = Utils.segClean(candidate);
        if (targetWords.size() == 0 || candidateWords.size() == 0) {
            return 0D;
        }
        return doCalculateSentence(targetWords.toArray(new String[targetWords.size()]), candidateWords.toArray(new String[candidateWords.size()]));
    }

    /**
     * 采用具体算法计算相似度
     *
     * @param targetWords    目标语句分词集
     * @param candidateWords 候选语句分词集
     * @return 相似度值
     */
    protected abstract double doCalculateSentence(String[] targetWords, String[] candidateWords);


}
