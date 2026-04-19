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

import java.util.concurrent.TimeUnit;

/**
 * redis 基础服务
 *
 * @author Bigbird
 */
public interface IRedisService {

    /**
     * SET
     * <p>
     * 将值 value 关联到 key
     *
     * @param key   键
     * @param value 值
     */
    void set(String key, Object value);

    /**
     * SETEX
     * <p>
     * 将键 key 的值设置为 value ， 并设置键 key 的过期时间。
     * 如果键 key 已经存在， 那么 SETEX 命令将覆盖已有的值。
     *
     * @param key      键
     * @param value    值
     * @param expire   过期时间
     * @param timeUnit 时间单位
     */
    void set(String key, Object value, long expire, TimeUnit timeUnit);

    /**
     * GET
     * <p>
     * 返回与键 key 相关联的值。
     *
     * @param key   键
     * @return 值
     */
    String get(String key);

    /**
     * GET
     * <p>
     * 返回与键 key 相关联的值。
     *
     * @param key   键
     * @param clazz 值类型
     * @return 值
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 设置过期时间
     *
     * @param key      键
     * @param expire   过期时间
     * @param timeUnit 时间单位
     * @return 设置是否成功，当key不存在时，返回false，当key存在时，会更新过期时间
     */
    boolean expire(String key, long expire, TimeUnit timeUnit);

    /**
     * DEL
     * <p>
     * 删除给定的 key 。
     *
     * @param key 键
     * @return 如果 key 存在且被删除，返回 true
     */
    boolean del(String key);

    /**
     * SETNX
     * <p>
     * 只在键 key 不存在的情况下， 将键 key 的值设置为 value 。
     * 若键 key 已经存在， 则 SETNX 命令不做任何动作。
     *
     * @param key   键
     * @param value 值
     * @return 若已经存在 key 返回 false。
     */
    boolean setnx(String key, Object value);

    /**
     * SETNXEX
     * <p>
     * 只在键 key 不存在的情况下， 将键 key 的值设置为 value，并且设置过期时间。
     * 若键 key 已经存在， 则 SETNX 命令不做任何动作。
     *
     * @param key      键
     * @param value    值
     * @param expire   过期时间
     * @param timeUnit 时间单位
     * @return 若已经存在 key 返回 false。
     */
    boolean setnxex(String key, Object value, long expire, TimeUnit timeUnit);

    /**
     * INCR
     * <p>
     * 为键 key 储存的数字值加上一。
     *
     * @param key 键
     * @return 计算后的值
     */
    long incr(String key);

    /**
     * INCRBY
     * <p>
     * 为键 key 储存的数字值加上 delta。
     *
     * @param key   键
     * @param delta 偏移量
     * @return 计算后的值
     */
    long incrby(String key, long delta);

    /**
     * DECR
     * <p>
     * 为键 key 储存的数字值减去一。
     *
     * @param key 键
     * @return 计算后的值
     */
    long decr(String key);

    /**
     * DECRBY
     * <p>
     * 为键 key 储存的数字值减去 delta。
     *
     * @param key   键
     * @param delta 偏移量
     * @return 计算后的值
     */
    long decrby(String key, long delta);

    /**
     * TTL
     * <p>
     * 返回给定 key 的剩余生存时间(TTL, time to live)。
     *
     * @param key 键
     * @return 当 key 不存在时，返回 -2 。
     * 当 key 存在但没有设置剩余生存时间时，返回 -1 。
     * 否则返回 key 的剩余生存时间。
     */
    long ttl(String key);

    /**
     * EXISTS
     * <p>
     * 检查给定 key 是否存在。
     *
     * @param key 键
     * @return 返回是否存在
     */
    boolean exists(String key);
}
