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
package wang.bigbird.domain.framework.cache.support.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import wang.bigbird.domain.framework.cache.config.property.CacheProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import wang.bigbird.domain.framework.cache.support.CustomizedCacheManager;

import java.util.concurrent.TimeUnit;

/**
 * 基于Caffeine的缓存管理器，
 * 可实现根据缓存名称调整缓存过期时间
 *
 * @author Bigbird
 */
public class CustomizedCaffeineCacheManager extends CustomizedCacheManager {

    private CacheProperties cacheProperties;

    public CustomizedCaffeineCacheManager(CacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
    }

    /**
     * 从配置中读取默认配置
     *
     * @param name
     * @return
     */
    @Override
    protected Cache getMissingCache(String name) {
        CacheProperties.Caffeine caffeineCacheProperties = cacheProperties.getCaffeine();
        String spec = caffeineCacheProperties.getSpec();
        Caffeine<Object, Object> caffeine;
        if (StringUtils.isNotBlank(spec)) {
            caffeine = Caffeine.from(CaffeineSpec.parse(spec));
        } else {
            caffeine = Caffeine.newBuilder()
                    .recordStats()
                    .expireAfterWrite(parseTTL(name, caffeineCacheProperties.getTimeToLive().toMillis()), TimeUnit.MILLISECONDS);
        }
        long ttl = parseTTL(name, -1);
        if (ttl >= 0) {
            caffeine.expireAfterWrite(ttl, TimeUnit.MILLISECONDS);
        }
        return new CaffeineCache(name, caffeine.build());
    }
}
