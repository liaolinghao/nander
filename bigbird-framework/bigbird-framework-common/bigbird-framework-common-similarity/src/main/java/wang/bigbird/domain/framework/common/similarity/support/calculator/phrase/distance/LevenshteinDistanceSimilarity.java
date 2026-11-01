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
package wang.bigbird.domain.framework.common.similarity.support.calculator.phrase.distance;

import org.apache.commons.text.similarity.LevenshteinDistance;
import wang.bigbird.domain.framework.common.similarity.support.calculator.phrase.AbstractPhraseSimilarity;

/**
 * 基于莱文斯坦编辑距离的相似度计算
 * 统计两个短语之间增、删、替换字符的最少操作次数，
 * 再基于最大字符串长度做归一化，得到 [0,1] 相似度分值；
 * 分值越高代表字符编辑代价越小
 *
 * @author Bigbird
 */
public class LevenshteinDistanceSimilarity extends AbstractPhraseSimilarity {

    private static final LevenshteinDistance LEV_DIST = new LevenshteinDistance();

    private LevenshteinDistanceSimilarity() {
    }

    private static class Holder {
        private static final LevenshteinDistanceSimilarity INSTANCE = new LevenshteinDistanceSimilarity();
    }

    public static LevenshteinDistanceSimilarity getInstance() {
        return LevenshteinDistanceSimilarity.Holder.INSTANCE;
    }

    @Override
    protected double doCalculatePhrase(String target, String candidate) {
        int dist = LEV_DIST.apply(target, candidate);
        int maxLen = Math.max(target.length(), candidate.length());
        return 1.0 - (double) dist / maxLen;
    }

}
