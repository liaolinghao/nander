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
package wang.bigbird.domain.framework.data.elasticsearch.base.enums;

/**
 * 分词方法
 *
 * @author Bigbird
 */
public enum AnalyzerEnum {

    /**
     * ik_smart分词
     */
    ik_smart("ik_smart"),
    /**
     * ik_max_word分词
     */
    ik_max_word("ik_max_word");

    private String name;

    AnalyzerEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
