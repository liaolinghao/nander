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
package wang.bigbird.domain.framework.common.forbidden.service.base;

import java.util.List;

/**
 * 检测禁用词服务
 *
 * @author Bigbird
 */
public interface IForbidWordService {

    /**
     * 文本是否包含禁用词
     *
     * @param text the text
     * @return the boolean
     */
    boolean include(final String text);

    /**
     * 文本包含的禁用词数量
     *
     * @param text the text
     * @return the int
     */
    int forbidWordCount(final String text);

    /**
     * 文本包含的禁用词列表
     *
     * @param text the text
     * @return the list
     */
    List<String> forbidWordList(final String text);

    /**
     * 将文本中包含的禁用词的每个字符以指定符号代替
     *
     * @param text   the text
     * @param symbol the symbol
     * @return the string
     */
    String replace(final String text, final char symbol);

    /**
     * 增加禁用词，通常基于文件的禁用词库不支持持久化，基于数据库的可以支持
     *
     * @param refreshNow 成功后是否立即刷新
     * @param words      the words
     * @return 是否成功
     */
    boolean addForbidWord(boolean refreshNow, String... words);

    /**
     * 删除禁用词，通常基于文件的禁用词库不支持持久化，基于数据库的可以支持
     *
     * @param refreshNow 成功后是否立即刷新
     * @param words      the words
     * @return 是否成功
     */
    boolean removeForbidWord(boolean refreshNow, String... words);

}
