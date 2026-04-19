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
package wang.bigbird.domain.framework.data.redis.service.base;

import java.util.List;

/**
 * redis 有序set 服务
 *
 * @author Bigbird
 */
public interface IRedisSortedSetService {

    /**
     * ZADD
     * <p>
     * 将一个 member 元素及其 score 值加入到有序集 key 当中。
     * 如果某个 member 已经是有序集的成员，那么更新这个 member 的 score 值，并通过重新插入这个 member 元素，来保证该 member 在正确的位置上。
     *
     * @param key   键
     * @param score 分数
     * @param value 值
     * @return 添加是否成功
     */
    boolean zadd(String key, double score, Object value);

    /**
     * ZSCORE
     * <p>
     * 返回有序集 key 中，成员 member 的 score 值。
     *
     * @param key   键
     * @param value 值
     * @return 返回分数，不存在则返回 null。
     */
    Double zscore(String key, Object value);

    /**
     * ZINCRBY
     * <p>
     * 为有序集 key 的成员 member 的 score 值加上增量或减量。
     *
     * @param key   键
     * @param value 值
     * @param delta 增减量
     * @return 返回更新后的分数
     */
    Double zincrby(String key, Object value, Number delta);

    /**
     * ZCARD
     * <p>
     * 返回有序集元素的数量
     *
     * @param key 键
     * @return 有序集元素的数量
     */
    int zcard(String key);

    /**
     * ZCOUNT
     * <p>
     * 返回有序集 key 中， score 值在 startScore 和 endScore 之间（闭区间）的成员的数量。
     *
     * @param key        键
     * @param startScore 开始分数
     * @param endScore   结束分数
     * @return 符合条件的成员数量
     */
    int zcount(String key, double startScore, double endScore);

    /**
     * ZRANGE
     * <p>
     * 返回有序集 key 中，指定区间内的成员。
     * 其中成员的位置按 score 值递增（从小到大）来排序。
     *
     * @param key   键
     * @param start 起始索引
     * @param end   结束索引
     * @param clazz 类型
     * @param <T>
     * @return 指定区间内，有序集成员的列表
     */
    <T> List<T> zrange(String key, int start, int end, Class<T> clazz);

    /**
     * ZREVRANGE
     * <p>
     * 返回有序集 key 中，指定区间内的成员。
     * 其中成员的位置按 score 值递减（从大到小）来排序。
     *
     * @param key 键
     * @param start 起始索引
     * @param end 结束索引
     * @param clazz 类型
     * @param <T>
     * @return 指定区间内，有序集成员的列表
     */
    <T> List<T> zrevrange(String key, int start, int end, Class<T> clazz);

    /**
     * ZRANGE
     * <p>
     * 返回有序集 key 中，指定分数区间内的成员。
     * 其中成员的位置按 score 值递增（从小到大）来排序。
     *
     * @param key        键
     * @param startScore 开始分数
     * @param endScore   结束分数
     * @param clazz      类型
     * @return 指定分数区间内，有序集成员的列表
     */
    <T> List<T> zrange(String key, double startScore, double endScore, Class<T> clazz);

    /**
     * ZREVRANGE
     * <p>
     * 返回有序集 key 中，指定分数区间内的成员。
     * 其中成员的位置按 score 值递减（从大到小）来排序。
     *
     * @param key        键
     * @param startScore 开始分数
     * @param endScore   结束分数
     * @param clazz      类型
     * @return 指定分数区间内，有序集成员递减的列表
     */
    <T> List<T> zrevrange(String key, double startScore, double endScore, Class<T> clazz);

    /**
     * ZRANGEBYSCORE
     * <p>
     * 返回有序集 key 中，指定分数区间内的成员。
     * 其中成员的位置按 score 值递增（从小到大）来排序。
     * 可进行分页查询。
     *
     * @param key        键
     * @param startScore 开始分数
     * @param endScore   结束分数
     * @param offset     偏移位置
     * @param count      数量
     * @param clazz      类型
     * @return 指定分数区间内，有序集成员的列表
     */
    <T> List<T> zrangebypage(String key, double startScore, double endScore, int offset, int count, Class<T> clazz);

    /**
     * ZREVRANGEBYSCORE
     * <p>
     * 返回有序集 key 中，指定分数区间内的成员。
     * 其中成员的位置按 score 值递减（从大到小）来排序。
     * 可进行分页查询。
     *
     * @param key        键
     * @param startScore 开始分数
     * @param endScore   结束分数
     * @param offset     偏移位置
     * @param count      数量
     * @param clazz      类型
     * @return 指定分数区间内，有序集成员的列表
     */
    <T> List<T> zrevrangebypage(String key, double startScore, double endScore, int offset, int count, Class<T> clazz);

    /**
     * ZRANK
     * <p>
     * 返回有序集 key 中成员 member 的排名。
     * 其中有序集成员按 score 值递增（从小到大）顺序排列。
     * 排名以 0 为底，也就是说， score 值最小的成员排名为 0 。
     *
     * @param key   键
     * @param value 值
     * @return 排名，如果没有值则返回 null。
     */
    Integer zrank(String key, Object value);

    /**
     * ZREVRANK
     * <p>
     * 返回有序集 key 中成员 member 的排名。
     * 其中有序集成员按 score 值递减（从大到小）顺序排列。
     * 排名以 0 为底，也就是说， score 值最大的成员排名为 0 。
     *
     * @param key   键
     * @param value 值
     * @return 排名，如果没有值则返回 null。
     */
    Integer zrevrank(String key, Object value);

    /**
     * ZREM
     * <p>
     * 移除有序集中的成员。
     *
     * @param key   键
     * @param value 值
     * @return 移除是否成功
     */
    boolean zrem(String key, Object value);

    /**
     * ZREMRANGEBYRANK
     * <p>
     * 移除有序集 key 中，指定排名（rank）区间内的所有成员。
     *
     * @param key        键
     * @param startIndex 开始排名
     * @param endIndex   结束排名
     * @return 被移除的元素个数
     */
    int zremrangebyrank(String key, int startIndex, int endIndex);

    /**
     * ZREMRANGEBYSCORE
     * <p>
     * 移除有序集 key 中，指定分数（score）区间内的所有成员。
     *
     * @param key        键
     * @param startScore 开始分数
     * @param endScore   结束分数
     * @return 被移除的元素个数
     */
    int zremrangebyscore(String key, double startScore, double endScore);
}
