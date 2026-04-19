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
 * ES的搜索方式
 *
 * @author Bigbird
 */
public enum QueryTypeEnum {

    /**
     * 适合搜索text类型字段，会将搜索词分词，然后判断match的分词结果和text的分词结果是否有相同的，存在交集就匹配
     */
    match,
    /**
     * 如果用于keyword类型字段，必须完全一致（等同于term），如果用于text类型字段，match_phrase的分词结果必须在text字段分词中都包含，而且顺序必须相同，而且必须都是连续的。
     */
    match_phrase,
    /**
     * 和 match_phrase 用法是一样的，区别就在于它允许对最后一个词条前缀匹配
     */
    match_phrase_prefix,
    /**
     * 完全一致
     */
    term,
    /**
     * 前缀匹配
     */
    prefix

}
