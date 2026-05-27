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
import java.util.Set;

/**
 * redis set 服务
 *
 * @author Bigbird
 */
public interface IRedisSetService {

    /**
     * SADD
     * <p>
     * 将一个 member 元素加入到集合 key 当中，已经存在于集合的 member 元素将被忽略。
     * 假如 key 不存在，则创建一个只包含 member 元素作成员的集合。
     *
     * @param key   键
     * @param value 值
     * @return 操作是否成功
     */
    boolean sadd(String key, Object value);

    /**
     * SADD
     * <p>
     * 将一批 member 元素加入到集合 key 当中，已经存在于集合的 member 元素将被忽略。
     * 假如 key 不存在，则创建一个包含 member 元素作成员的集合。
     *
     * @param key  键
     * @param objs 值
     * @return 操作是否成功
     */
    boolean sadd(String key, Set<?> objs);

    /**
     * SISMEMBER
     * <p>
     * 判断 member 元素是否存在于集合 key 当中。
     *
     * @param key   键
     * @param value 值
     * @return 元素是否存在
     */
    boolean sismember(String key, Object value);

    /**
     * SPOP
     * <p>
     * 移除并返回集合中的一个随机元素。
     *
     * @param key   键
     * @param clazz 元素类型
     * @return 返回集合中的一个随机元素
     */
    <T> T spop(String key, Class<T> clazz);

    /**
     * SREM
     * <p>
     * 移除集合中的一个元素，不存在的元素会被忽略。
     *
     * @param key   键
     * @param value 值
     * @return 移除是否成功
     */
    boolean srem(String key, Object value);

    /**
     * SMOVE
     * <p>
     * 将 member 元素从 source 集合移动到 destination 集合。
     * 集合不存在或不包含指定的 member 元素，则不执行任何操作
     *
     * @param key     源键
     * @param destKey 目标键
     * @param value   值
     * @return 移动是否成功
     */
    boolean smove(String key, String destKey, Object value);

    /**
     * SCARD
     * <p>
     * 集合中元素的数量。
     *
     * @param key 键
     * @return 集合中元素的数量
     */
    int scard(String key);

    /**
     * SMEMBERS
     * <p>
     * 返回集合 key 中的所有成员。
     *
     * @param key   键
     * @param clazz 类型
     * @return 集合中的所有成员
     */
    <T> Set<T> smembers(String key, Class<T> clazz);

    /**
     * SINTER
     * <p>
     * 返回一个集合的全部成员，该集合是所有给定集合的交集。
     * 当给定集合当中有一个空集时，结果也为空集（根据集合运算定律）。
     *
     * @param keys  键列表
     * @param clazz 类型
     * @return 交集成员的列表
     */
    <T> Set<T> sinter(List<String> keys, Class<T> clazz);

    /**
     * SUNION
     * <p>
     * 返回一个集合的全部成员，该集合是所有给定集合的并集。
     *
     * @param keys  键列表
     * @param clazz 类型
     * @return 并集成员的列表
     */
    <T> Set<T> sunion(List<String> keys, Class<T> clazz);

    /**
     * SDIFF
     * <p>
     * 返回第一个集合与之后给定集合之间的差集。
     *
     * @param keys  键列表
     * @param clazz 类型
     * @return 差集成员的列表
     */
    <T> Set<T> sdiff(List<String> keys, Class<T> clazz);
}
