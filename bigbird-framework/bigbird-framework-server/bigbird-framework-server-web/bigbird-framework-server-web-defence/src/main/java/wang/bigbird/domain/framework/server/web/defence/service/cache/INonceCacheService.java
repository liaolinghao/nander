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
package wang.bigbird.domain.framework.server.web.defence.service.cache;

/**
 * 随机数缓存服务
 *
 * @author Bigbird
 */
public interface INonceCacheService {

    /**
     * 如果缓存存在，则返回缓存中的值，如果不存在则返回null
     *
     * @param nonce 缓存值
     * @return 缓存值
     */
    String get(String nonce);

    /**
     * 加入缓存
     *
     * @param nonce 缓存值
     */
    String put(String nonce);

}
