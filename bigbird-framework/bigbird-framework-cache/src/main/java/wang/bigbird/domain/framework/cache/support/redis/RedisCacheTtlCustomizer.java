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
package wang.bigbird.domain.framework.cache.support.redis;

import lombok.Data;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * @author Bigbird
 */
@Data
@Order
public class RedisCacheTtlCustomizer implements RedisCacheManagerBuilderCustomizer {

    /**
     * CacheName 对应的 TTL（PT 时间格式）
     */
    private Map<String, Duration> cacheNameTimeToLiveMap = Collections.emptyMap();

    @Override
    public void customize(RedisCacheManager.RedisCacheManagerBuilder builder) {
        RedisCacheConfiguration defaultCacheConfiguration = RedisCacheCustomizerUtils.getDefaultCacheConfigurationFor(builder);
        cacheNameTimeToLiveMap.forEach((cacheName, ttl) -> {
            RedisCacheConfiguration configuration = defaultCacheConfiguration.entryTtl(ttl);
            builder.withCacheConfiguration(cacheName, configuration);
        });
    }

}
