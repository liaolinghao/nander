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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * redis hash 服务
 *
 * @author Bigbird
 */
public interface IRedisHashService {

    /**
     * HSET
     * <p>
     * 将哈希表 hash 中域 field 的值设置为 value 。
     * 如果给定的哈希表并不存在， 那么一个新的哈希表将被创建并执行 HSET 操作。
     * 如果域 field 已经存在于哈希表中， 那么它的旧值将被新值 value 覆盖。
     *
     * @param key   键
     * @param field 域
     * @param value 值
     * @return 当在哈希表中新创建 field 域并成功为它设置值时，命令返回 1 ；如果域 field 已经存在于哈希表，并且 HSET 命令成功使用新值覆盖了它的旧值，那么命令返回 0 。
     */
    int hset(String key, String field, Object value);

    /**
     * HSETNX
     * <p>
     * 当且仅当域 field 尚未存在于哈希表的情况下，将它的值设置为 value 。
     * 如果给定域已经存在于哈希表当中，那么命令将放弃执行设置操作。
     * 如果哈希表hash不存在，那么一个新的哈希表将被创建并执行 HSETNX 命令。
     *
     * @param key   键
     * @param field 域
     * @param value 值
     * @return 在设置成功时返回 true，在给定域已经存在而放弃执行设置操作时返回 false。
     */
    boolean hsetnx(String key, String field, Object value);

    /**
     * HGET
     * <p>
     * 返回哈希表中给定域的值。
     *
     * @param key   键
     * @param field 域
     * @param clazz 值类型
     * @return 返回给定域的值
     */
    <T> T hget(String key, String field, Class<T> clazz);

    /**
     * HEXISTS
     * <p>
     * 检查给定域 field 是否存在于哈希表 hash 当中。
     *
     * @param key   键
     * @param field 域
     * @return 给定域存在时返回 true, 在给定域不存在时返回 false。
     */
    boolean hexists(String key, String field);

    /**
     * HDEL
     * <p>
     * 删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略。
     *
     * @param key    键
     * @param fields 域
     * @return 被成功移除的域的数量，不包括被忽略的域。
     */
    int hdel(String key, String... fields);

    /**
     * HLEN
     * <p>
     * 返回哈希表 key 中域的数量。
     *
     * @param key 键
     * @return 哈希表中域的数量。
     */
    int hlen(String key);

    /**
     * HINCRBY
     * <p>
     * 为哈希表 key 中的域 field 的值加上增量 increment 。
     * 增量也可以为负数，相当于对给定域进行减法操作。
     * 如果 key 不存在，一个新的哈希表被创建并执行 HINCRBY 命令。
     * 如果域 field 不存在，那么在执行命令前，域的值被初始化为 0 。
     * 对一个储存字符串值的域 field 执行 HINCRBY 命令将造成一个错误。
     * 本操作的值被限制在 64 位(bit)有符号数字表示之内。
     *
     * @param key   键
     * @param field 域
     * @param delta 偏移量
     * @return 执行加法操作之后 field 域的值。
     */
    Number hincrby(String key, String field, Number delta);

    /**
     * HMSET
     * <p>
     * 同时将多个 field-value (域-值)对设置到哈希表 key 中。
     * 此命令会覆盖哈希表中已存在的域。
     * 如果 key 不存在，一个空哈希表被创建并执行 HMSET 操作。
     *
     * @param key           键
     * @param fieldValueMap 域、值 Map
     */
    void hmset(String key, Map<String, Object> fieldValueMap);

    /**
     * HMGET
     * <p>
     * 用于返回哈希表中，一个或多个给定字段的值。
     * 如果指定的字段不存在于哈希表，那么返回一个 null 值。
     *
     * @param key    键
     * @param fields 域列表
     * @param clazz  值类型
     * @return 一个或多个给定字段的值，不存在则返回 null
     */
    <T> Map<String, T> hmget(String key, Collection<String> fields, Class<T> clazz);

    /**
     * HGETALL
     * <p>
     * 用于返回哈希表中，所有的字段和值。
     *
     * @param key   键
     * @param clazz 值类型
     * @return 所有的字段和值，不存在则返回 null
     */
    <T> Map<String, T> hgetall(String key, Class<T> clazz);

    /**
     * HKEYS
     * <p>
     * 返回哈希表 key 中的所有域。
     *
     * @param key 键
     * @return 一个包含哈希表中所有域的表。当 key 不存在时，返回一个空表。
     */
    Set<String> hkeys(String key);
}
