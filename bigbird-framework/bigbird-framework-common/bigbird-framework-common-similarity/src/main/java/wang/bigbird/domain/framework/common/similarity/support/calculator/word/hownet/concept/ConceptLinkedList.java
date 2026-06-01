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
package wang.bigbird.domain.framework.common.similarity.support.calculator.word.hownet.concept;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;

/**
 * 概念处理列
 *
 * @author Bigbird
 */
@Slf4j
public class ConceptLinkedList extends LinkedList<Concept> {

    /**
     * 移除尾部指定数量的数据
     *
     * @param size 移除数据量
     */
    public void removeLast(int size) {
        for (int i = 0; i < size; i++) {
            removeLast();
        }
    }

    /**
     * 添加定义不重复的概念
     *
     * @param concept
     */
    public void addByDefine(Concept concept) {
        for (Concept c : this) {
            if (c.getDefine().equals(concept.getDefine())) {
                return;
            }
        }
        add(concept);
    }

}
