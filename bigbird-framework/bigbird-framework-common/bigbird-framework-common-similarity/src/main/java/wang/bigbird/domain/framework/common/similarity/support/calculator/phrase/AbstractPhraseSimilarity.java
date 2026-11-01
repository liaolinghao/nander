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
package wang.bigbird.domain.framework.common.similarity.support.calculator.phrase;

import wang.bigbird.domain.framework.common.similarity.support.calculator.AbstractSimilarity;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

/**
 * 处理短语层面的相似度计算
 *
 * @author Bigbird
 */
public abstract class AbstractPhraseSimilarity extends AbstractSimilarity {

    @Override
    protected double doCalculate(String target, String candidate) {
        // 比较之前需要去除短语中的空白字符
        target = StringUtils.compress(target);
        candidate = StringUtils.compress(candidate);
        return doCalculatePhrase(target, candidate);
    }

    /**
     * 采用具体算法计算相似度
     *
     * @param target    目标文本
     * @param candidate 候选文本
     * @return 相似度值
     */
    protected abstract double doCalculatePhrase(String target, String candidate);

}
