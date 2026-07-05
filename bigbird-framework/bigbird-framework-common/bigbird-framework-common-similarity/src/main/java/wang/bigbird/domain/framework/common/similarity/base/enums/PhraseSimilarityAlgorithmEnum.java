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
package wang.bigbird.domain.framework.common.similarity.base.enums;

import lombok.Getter;
import wang.bigbird.domain.framework.core.base.util.enums.ValuedEnum;

/**
 * 短语相似度算法类型枚举
 *
 * @author Bigbird
 */
@Getter
public enum PhraseSimilarityAlgorithmEnum implements ValuedEnum<String> {

    /**
     * 莱文斯坦归一化相似度（基于编辑距离）
     * 适用于拼写纠错、错别字匹配、DNA比对或需要距离度量性质的场景
     */
    LevenshteinDistance("莱文斯坦归一化相似度"),
    /**
     * JaroWinkler 短字符串优化相似度
     * 适用于开头必须严格（如：人名、地名，尤其是考虑开头的一致性）的场景
     */
    JaroWinklerDistance("JaroWinkler 短字符串优化相似度"),
    /**
     * Trigram 杰卡德相似度
     * 适合在百万条数据中快速搜索近似记录
     */
    TrigramJaccard("Trigram 杰卡德相似度"),
    /**
     * 余弦相似度
     * 适合招聘岗位、商品名称、长短语匹配
     */
    Cosine("余弦相似度");

    private final String description;

    PhraseSimilarityAlgorithmEnum(String description) {
        this.description = description;
    }

    /**
     * 获取短语相似度算法类型枚举对象
     *
     * @param code 短语相似度算法类型代码
     * @return 短语相似度算法类型枚举对象
     */
    public static PhraseSimilarityAlgorithmEnum getInstanceByCode(String code) {
        for (PhraseSimilarityAlgorithmEnum psae : PhraseSimilarityAlgorithmEnum.values()) {
            if (psae.name().equalsIgnoreCase(code)) {
                return psae;
            }
        }
        return null;
    }

    @Override
    public String value() {
        return name();
    }

}
