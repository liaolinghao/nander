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
package wang.bigbird.domain.framework.common.similarity.support.calculator.word.cilin;

import wang.bigbird.domain.framework.common.similarity.support.calculator.word.AbstractWordSimilarity;

import java.util.Set;

/**
 * 词林编码的相似度计算
 *
 * @author Bigbird
 */
public class CilinSimilarity extends AbstractWordSimilarity {

    private static CilinSimilarity instance = null;

    public static CilinSimilarity getInstance() {
        if (instance == null) {
            instance = new CilinSimilarity();
        }
        return instance;
    }

    private CilinSimilarity() {
    }

    @Override
    protected double doCalculate(String target, String candidate) {
        double sim = 0.0;
        Set<String> codeSet1 = CilinDictionary.getInstance().getCilinCodes(target);
        Set<String> codeSet2 = CilinDictionary.getInstance().getCilinCodes(candidate);
        if (codeSet1 == null || codeSet2 == null) {
            return 0.0;
        }
        // 根据编码计算相似度，取两个编码之间的最大相似度值
        for (String code1 : codeSet1) {
            for (String code2 : codeSet2) {
                double s = getSimilarityByCode(code1, code2);
                if (sim < s) {
                    sim = s;
                }
            }
        }
        return sim;
    }

    public double getSimilarityByCode(String code1, String code2) {
        return CilinCode.calculateCommonWeight(code1, code2) / CilinCode.TOTAL_WEIGHT;
    }

}
