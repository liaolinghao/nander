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
 * redis list 服务
 *
 * @author Bigbird
 */
public interface IRedisListService {

    /**
     * lpush
     * <p>
     * 将值 value 放入 key 对应的列表头部
     *
     * @param key   键
     * @param value 值
     */
    void lpush(String key, Object value);

    /**
     * lpop
     * <p>
     * 将 key 对应的列表头部的值取出
     *
     * @param key   键
     * @param clazz 值类型
     * @return 列表的第一个元素
     */
    <T> T lpop(String key, Class<T> clazz);

    /**
     * rpush
     * <p>
     * 将值 value 放入 key 对应的列表尾部
     *
     * @param key   键
     * @param value 值
     */
    void rpush(String key, Object value);

    /**
     * rpop
     * <p>
     * 将 key 对应的列表尾部的值取出
     *
     * @param key   键
     * @param clazz 值类型
     * @return 列表的最后一个元素
     */
    <T> T rpop(String key, Class<T> clazz);

    /**
     * lindex
     * <p>
     * 将 key 对应的列表指定位置的值取出
     *
     * @param key   键
     * @param index 位置索引
     * @param clazz 值类型
     * @return 列表指定位置的元素
     */
    <T> T lindex(String key, int index, Class<T> clazz);

    /**
     * llen
     * <p>
     * 获取 key 对应的列表长度
     *
     * @param key 键
     * @return 列表的长度
     */
    int llen(String key);

    /**
     * lrange
     * <p>
     * 将 key 对应的列表指定位置范围的值取出
     *
     * @param key   键
     * @param start 起始位置索引
     * @param end   结束位置索引
     * @param clazz 值类型
     * @return 列表指定位置范围的元素集合
     */
    <T> List<T> lrange(String key, int start, int end, Class<T> clazz);


    /**
     * lrem
     * <p>
     * 根据参数 count 的值，移除 key 对应的列表中与参数 value 相等的元素
     *
     * @param key   键
     * @param count count > 0 : 从表头开始向表尾搜索，移除与 VALUE 相等的元素，数量为 COUNT 。
     *              count < 0 : 从表尾开始向表头搜索，移除与 VALUE 相等的元素，数量为 COUNT 的绝对值。
     *              count = 0 : 移除表中所有与 VALUE 相等的值。
     * @param value 值
     * @return 操作是否成功
     */
    boolean lrem(String key, int count, Object value);

    /**
     * rpoplpush
     * <p>
     * 移除 sourceKey 列表的最后一个元素，并将该元素添加到 destKey 对应列表的头部并返回
     *
     * @param sourceKey 键
     * @param destKey   键
     * @param clazz     值类型
     * @return 列表 sourceKey 中的最后一个元素
     */
    <T> T rpoplpush(String sourceKey, String destKey, Class<T> clazz);
}
