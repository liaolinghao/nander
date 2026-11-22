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
 * 段落相似度算法类型枚举
 *
 * @author Bigbird
 */
@Getter
public enum ParagraphSimilarityAlgorithmEnum implements ValuedEnum<String> {

    /**
     * 分层对齐相似度
     * 适用于段落有多个句子、句子数量不对齐、语序有参考价值、需要可解释对齐明细的段落级模糊匹配
     */
    LayeredAlignment("分层对齐相似度");

    private final String description;

    ParagraphSimilarityAlgorithmEnum(String description) {
        this.description = description;
    }

    /**
     * 获取段落相似度算法类型枚举对象
     *
     * @param code 段落相似度算法类型代码
     * @return 段落相似度算法类型枚举对象
     */
    public static ParagraphSimilarityAlgorithmEnum getInstanceByCode(String code) {
        for (ParagraphSimilarityAlgorithmEnum psae : ParagraphSimilarityAlgorithmEnum.values()) {
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
