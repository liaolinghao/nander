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
 * 词语相似度算法类型枚举
 *
 * @author Bigbird
 */
@Getter
public enum WordSimilarityAlgorithmEnum implements ValuedEnum<String> {

    /**
     * 词林相似度（基于词林语义词典）
     * 适用于语义相关的词语比较
     */
    CILIN("词林相似度"),

    /**
     * 概念相似度（基于知网概念层级）
     * 适用于概念相关的词语比较
     */
    CONCEPT("知网概念相似度"),

    /**
     * 拼音相似度（基于拼音匹配）
     * 适用于拼音相同的词语比较
     */
    PINYIN("拼音相似度"),

    /**
     * 字面相似度（基于字符匹配）
     * 适用于字符级别的比较，无需词典
     */
    CHARACTER("字面相似度");

    private final String description;

    WordSimilarityAlgorithmEnum(String description) {
        this.description = description;
    }

    /**
     * 获取词语相似度算法类型枚举对象
     *
     * @param code 词语相似度算法类型代码
     * @return 词语相似度算法类型枚举对象
     */
    public static WordSimilarityAlgorithmEnum getInstanceByCode(String code) {
        for (WordSimilarityAlgorithmEnum wsae : WordSimilarityAlgorithmEnum.values()) {
            if (wsae.name().equalsIgnoreCase(code)) {
                return wsae;
            }
        }
        return null;
    }

    @Override
    public String value() {
        return name();
    }

}
