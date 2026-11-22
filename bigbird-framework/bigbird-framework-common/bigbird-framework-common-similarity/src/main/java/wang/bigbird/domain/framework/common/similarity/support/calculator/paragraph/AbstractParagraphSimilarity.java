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
package wang.bigbird.domain.framework.common.similarity.support.calculator.paragraph;

import wang.bigbird.domain.framework.common.similarity.base.tool.ParagraphSplitter;
import wang.bigbird.domain.framework.common.similarity.support.calculator.AbstractSimilarity;

import java.util.List;

/**
 * 处理段落层面的相似度计算
 *
 * @author Bigbird
 */
public abstract class AbstractParagraphSimilarity extends AbstractSimilarity {

    @Override
    protected double doCalculate(String target, String candidate) {
        // 分词与清洗，后续所有计算都只在"承载语义的词"上进行
        List<String> targetSentences = ParagraphSplitter.split(target);
        List<String> candidateSentences = ParagraphSplitter.split(candidate);
        if (targetSentences.size() == 0 || candidateSentences.size() == 0) {
            return 0D;
        }
        return doCalculateParagraph(targetSentences, candidateSentences);
    }

    /**
     * 采用具体算法计算相似度
     *
     * @param targetSentences    目标段落语句集
     * @param candidateSentences 候选段落语句集
     * @return 相似度值
     */
    protected abstract double doCalculateParagraph(List<String> targetSentences, List<String> candidateSentences);

}
