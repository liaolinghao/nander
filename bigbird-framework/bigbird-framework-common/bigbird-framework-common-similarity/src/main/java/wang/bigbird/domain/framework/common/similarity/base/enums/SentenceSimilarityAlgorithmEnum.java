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
 * 语句相似度算法类型枚举
 *
 * @author Bigbird
 */
@Getter
public enum SentenceSimilarityAlgorithmEnum implements ValuedEnum<String> {

    /**
     * 语义相似度（基于词形（词面匹配） 与词序（逆序关系））
     * 适用于指令匹配、问句识别、短文本意图判别，可容忍正常语序扰动，同义词识别能力有限
     */
    Semantic("语义相似度");

    private final String description;

    SentenceSimilarityAlgorithmEnum(String description) {
        this.description = description;
    }

    /**
     * 获取语句相似度算法类型枚举对象
     *
     * @param code 语句相似度算法类型代码
     * @return 语句相似度算法类型枚举对象
     */
    public static SentenceSimilarityAlgorithmEnum getInstanceByCode(String code) {
        for (SentenceSimilarityAlgorithmEnum ssae : SentenceSimilarityAlgorithmEnum.values()) {
            if (ssae.name().equalsIgnoreCase(code)) {
                return ssae;
            }
        }
        return null;
    }

    @Override
    public String value() {
        return name();
    }

}
