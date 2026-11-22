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
package wang.bigbird.domain.framework.common.similarity.support.strategy.paragraph.layered;

import wang.bigbird.domain.framework.common.similarity.base.enums.ParagraphSimilarityAlgorithmEnum;
import wang.bigbird.domain.framework.common.similarity.support.calculator.paragraph.layered.LayeredAlignmentSimilarity;
import wang.bigbird.domain.framework.common.similarity.support.strategy.paragraph.IParagraphSimilarityStrategy;

/**
 * 分层对齐相似度策略
 *
 * <p>
 * 基于粗筛（Trigram 杰卡德）、句子最优对齐（加权 Dice）与句序结构分（相邻逆序归一化）计算段落相似度
 * </p>
 *
 * @author Bigbird
 */
public class LayeredAlignmentSimilarityStrategy implements IParagraphSimilarityStrategy {

    @Override
    public ParagraphSimilarityAlgorithmEnum getAlgorithm() {
        return ParagraphSimilarityAlgorithmEnum.LayeredAlignment;
    }

    @Override
    public double calculate(String targetParagraph, String candidateParagraph) {
        return LayeredAlignmentSimilarity.getInstance().calculate(targetParagraph, candidateParagraph);
    }

}
