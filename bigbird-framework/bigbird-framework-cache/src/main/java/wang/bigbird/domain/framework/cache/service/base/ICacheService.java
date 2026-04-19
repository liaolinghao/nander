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
package wang.bigbird.domain.framework.cache.service.base;

/**
 * 缓存服务
 * <p>
 * 编程式处理缓存
 *
 * @author Bigbird
 */
public interface ICacheService {

    /**
     * 清理指定缓存的特定值
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     */
    void clearCache(String cacheName, Object key);

    /**
     * 清理指定缓存的所有值
     *
     * @param cacheName 缓存名称
     */
    void clearCache(String cacheName);

}
