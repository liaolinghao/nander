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
package wang.bigbird.domain.framework.common.similarity.service.base;

import wang.bigbird.domain.framework.common.similarity.base.enums.PhraseSimilarityAlgorithmEnum;

/**
 * 短语相似度服务
 *
 * @author Bigbird
 */
public interface IPhraseSimilarityService {

    /**
     * 使用指定算法计算短语相似度
     *
     * @param targetPhrase    目标短语
     * @param candidatePhrase 候选短语
     * @param algorithm       算法类型
     * @return 相似度值（0.0 - 1.0）
     */
    double calculate(String targetPhrase, String candidatePhrase, PhraseSimilarityAlgorithmEnum algorithm);

}
