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
package wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.AbstractCacheResolver;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.EnhancedCache;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.convert.EnhancedCacheConversionService;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * 包装一下Spring的{@link AbstractCacheResolver}
 * 将原本返回的Spring的{@link Cache}转成{@link EnhancedCache}
 *
 * @author Bigbird
 */
@RequiredArgsConstructor
public class EnhancedCachingResolverAdapter implements EnhancedCacheResolver {

    private final ConcurrentMap<Cache, EnhancedCache> enhancedCacheCache = new ConcurrentHashMap<>(1024);

    private final CacheResolver cacheResolver;

    private final EnhancedCacheConversionService conversionService;

    @Override
    public Collection<? extends EnhancedCache> resolveCaches(CacheOperationInvocationContext<?> context) {
        Collection<? extends Cache> caches = cacheResolver.resolveCaches(context);
        if (caches.isEmpty()) {
            return Collections.emptyList();
        }
        return caches.stream().map(this::convert).collect(Collectors.toList());
    }

    private EnhancedCache convert(Cache cache) {
        return enhancedCacheCache.computeIfAbsent(cache, conversionService::convert);
    }

}
