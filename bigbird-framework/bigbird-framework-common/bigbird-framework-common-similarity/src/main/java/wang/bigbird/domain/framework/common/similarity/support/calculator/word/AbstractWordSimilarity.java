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
package wang.bigbird.domain.framework.common.similarity.support.calculator.word;

import wang.bigbird.domain.framework.common.similarity.support.calculator.ISimilarity;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

/**
 * 处理词层面的相似度计算
 *
 * @author Bigbird
 */
public abstract class AbstractWordSimilarity implements ISimilarity {

    @Override
    public double calculate(String target, String candidate) {
        if (StringUtils.isBlank(target) && StringUtils.isBlank(candidate)) {
            return 1.0;
        }
        if (StringUtils.isBlank(target) || StringUtils.isBlank(candidate)) {
            return 0.0;
        }
        if (target.equalsIgnoreCase(candidate)) {
            return 1.0;
        }
        return doCalculate(target.trim(), candidate.trim());
    }

    /**
     * 采用具体算法计算相似度，如果存在集合之间的相似度计算，则取排列组合中的最大值
     *
     * @param target    目标文本
     * @param candidate 候选文本
     * @return 相似度值
     */
    protected abstract double doCalculate(String target, String candidate);

}
