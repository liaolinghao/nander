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
package wang.bigbird.domain.framework.common.similarity.support.calculator.word.pinyin;

import com.hankcs.hanlp.algorithm.EditDistance;
import wang.bigbird.domain.framework.common.similarity.support.calculator.word.AbstractWordSimilarity;
import wang.bigbird.domain.framework.core.base.util.DataUtils;

import java.util.Set;

/**
 * 拼音计算两个词相似度，拼音用编辑距离表示相似程度
 *
 * @author Bigbird
 */
public class PinyinSimilarity extends AbstractWordSimilarity {

    private static PinyinSimilarity instance = null;

    public static PinyinSimilarity getInstance() {
        if (instance == null) {
            instance = new PinyinSimilarity();
        }
        return instance;
    }

    private PinyinSimilarity() {
    }

    @Override
    public double doCalculate(String target, String candidate) {
        double max = 0.0;
        Set<String> pinyinSet1 = PinyinDictionary.getInstance().getPinyin(target);
        Set<String> pinyinSet2 = PinyinDictionary.getInstance().getPinyin(candidate);
        for (String pinyin1 : pinyinSet1) {
            for (String pinyin2 : pinyinSet2) {
                double distance = EditDistance.compute(pinyin1, pinyin2);
                double similarity = 1 - distance / (DataUtils.max(pinyin1.length(), pinyin2.length()).doubleValue());
                max = (max > similarity) ? max : similarity;
                if (DataUtils.approxEquals(max, 1.0)) {
                    return 1.0;
                }
            }
        }
        return max;
    }

}
