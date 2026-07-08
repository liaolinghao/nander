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
package wang.bigbird.domain.framework.common.similarity.support.strategy.phrase.distance;

import org.apache.commons.text.similarity.LevenshteinDistance;
import wang.bigbird.domain.framework.common.similarity.base.enums.PhraseSimilarityAlgorithmEnum;
import wang.bigbird.domain.framework.common.similarity.support.strategy.phrase.ISimilarityStrategy;

/**
 * 莱文斯坦归一化相似度策略
 * <p>
 * 计算从一个字符串转换到另一个字符串，最少需要多少次“单字符编辑操作”，
 * 精确量化了两个短语之间的”绝对修改代价”。
 * 公平、严谨，是所有文本相似度算法中最基础的“度量衡”。
 *
 * </p>
 *
 * @author Bigbird
 */
public class LevenshteinDistanceSimilarityStrategy implements ISimilarityStrategy {

    private static final LevenshteinDistance LEV_DIST = new LevenshteinDistance();

    @Override
    public PhraseSimilarityAlgorithmEnum getAlgorithm() {
        return PhraseSimilarityAlgorithmEnum.LevenshteinDistance;
    }

    @Override
    public double calculate(String targetPhrase, String candidatePhrase) {
        int dist = LEV_DIST.apply(candidatePhrase, targetPhrase);
        int maxLen = Math.max(targetPhrase.length(), candidatePhrase.length());
        return 1.0 - (double) dist / maxLen;
    }

}
