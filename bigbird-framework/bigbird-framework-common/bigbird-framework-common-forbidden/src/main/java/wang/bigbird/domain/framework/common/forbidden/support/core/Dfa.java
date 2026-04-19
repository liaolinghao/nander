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
package wang.bigbird.domain.framework.common.forbidden.support.core;

import java.util.Iterator;

/**
 * DFA算法的数据结构，将每个词用每个字符为key，构建嵌套的<key，map>结构，map中存放一个关键key=EOW，
 * 标识是否搜索到禁用词。对于禁用词，DFA是采用最小匹配原则，即如果 甲乙、甲乙丙都是禁用词，则检测到甲乙为止。
 *
 * @author Bigbird
 */
public interface Dfa {

    /**
     * 词完结的标志
     */
    String END_OF_WORD = "EOW";

    /**
     * 将词加到DFA的数据结构中
     *
     * @param words the words
     */
    void addWord(Iterator<String> words);

    /**
     * 新建一个空的实例
     *
     * @return the dfa
     */
    Dfa createNewEmpty();

    /**
     * 检测禁用词，从文本指定位置开始，检测到符合要求的第一个禁用词就结束检测
     *
     * @param text  the text
     * @param begin the begin
     * @return the flag index
     */
    FlagIndex getFlagIndex(final String text, final int begin);
}
