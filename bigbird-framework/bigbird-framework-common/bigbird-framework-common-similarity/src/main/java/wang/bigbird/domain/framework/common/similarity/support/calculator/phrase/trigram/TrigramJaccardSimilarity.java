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
package wang.bigbird.domain.framework.common.similarity.support.calculator.phrase.trigram;

import wang.bigbird.domain.framework.common.similarity.base.util.Utils;
import wang.bigbird.domain.framework.common.similarity.support.calculator.phrase.AbstractPhraseSimilarity;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;

import java.util.Set;

/**
 * 基于字符三元组集合交并比的相似度计算
 * 将短语切分为连续三元字符窗口（3-gram），分别得到两个短语的 trigram 集合，
 * 使用「交集数量 / 并集数量」得到杰卡德相似度 [0,1]；
 * 兼顾局部字符顺序特征，抗单点错别字
 *
 * @author Bigbird
 */
public class TrigramJaccardSimilarity extends AbstractPhraseSimilarity {

    private TrigramJaccardSimilarity() {
    }

    private static class Holder {
        private static final TrigramJaccardSimilarity INSTANCE = new TrigramJaccardSimilarity();
    }

    public static TrigramJaccardSimilarity getInstance() {
        return TrigramJaccardSimilarity.Holder.INSTANCE;
    }

    @Override
    protected double doCalculatePhrase(String target, String candidate) {
        Set<String> set1 = Utils.generateTrigramSet(target);
        Set<String> set2 = Utils.generateTrigramSet(candidate);
        return (double) CollectionUtils.intersect(set1, set2).size() / CollectionUtils.union(set1, set2).size();
    }

}

