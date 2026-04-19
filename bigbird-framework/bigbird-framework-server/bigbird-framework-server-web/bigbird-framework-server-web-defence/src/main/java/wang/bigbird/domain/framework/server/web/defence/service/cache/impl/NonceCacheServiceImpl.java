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
package wang.bigbird.domain.framework.server.web.defence.service.cache.impl;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.server.web.defence.service.cache.INonceCacheService;

/**
 * 随机数缓存服务
 *
 * @author Bigbird
 */
@Service
public class NonceCacheServiceImpl implements INonceCacheService {

    private static final String NONCE_CACHE_NAME = "DefenceNonceCache";

    @Override
    @Cacheable(value = NONCE_CACHE_NAME, key = "#nonce")
    public String get(String nonce) {
        return null;
    }

    @Override
    @CachePut(value = NONCE_CACHE_NAME, key = "#nonce")
    public String put(String nonce) {
        return nonce;
    }

}
