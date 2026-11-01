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

import org.apache.commons.text.similarity.JaroWinklerDistance;
import wang.bigbird.domain.framework.common.similarity.support.calculator.phrase.AbstractPhraseSimilarity;

/**
 * 基于 Jaro 相似度并针对短字符串前缀特征进行加权优化的相似度计算
 * 它在 Jaro 相似度的基础上，对两个字符串共同前缀给予额外奖励分，其设计初衷源于数据录入经验：
 * 人们在前 4 个字符上出错概率较低，且前缀匹配更能反映字符串的整体相似性。
 * 该算法输出 [0, 1] 区间的 double 值，值越高表示越相似，1.0 表示完全相同。
 *
 * @author Bigbird
 */
public class JaroWinklerDistanceSimilarity extends AbstractPhraseSimilarity {

    private static final JaroWinklerDistance JARO_WINKLER = new JaroWinklerDistance();

    private JaroWinklerDistanceSimilarity() {
    }

    private static class Holder {
        private static final JaroWinklerDistanceSimilarity INSTANCE = new JaroWinklerDistanceSimilarity();
    }

    public static JaroWinklerDistanceSimilarity getInstance() {
        return JaroWinklerDistanceSimilarity.Holder.INSTANCE;
    }

    @Override
    protected double doCalculatePhrase(String target, String candidate) {
        return JARO_WINKLER.apply(target, candidate);
    }

}


