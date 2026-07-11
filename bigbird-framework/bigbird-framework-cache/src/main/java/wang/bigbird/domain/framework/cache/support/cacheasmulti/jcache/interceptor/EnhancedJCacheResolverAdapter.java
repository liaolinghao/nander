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
package wang.bigbird.domain.framework.cache.support.cacheasmulti.jcache.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.EnhancedCache;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.interceptor.EnhancedCacheResolver;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.jcache.EnhancedJCacheCache;

import javax.cache.Cache;
import javax.cache.annotation.CacheInvocationContext;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 把JSR-107的{@link javax.cache.annotation.CacheResolver}包装成Spring的{@link CacheResolver}
 * 将原本返回的JSR-107的{@link Cache}转成{@link EnhancedCache}
 *
 * @author Bigbird
 */
@RequiredArgsConstructor
class EnhancedJCacheResolverAdapter implements EnhancedCacheResolver {

    private final ConcurrentMap<Cache<Object, Object>, EnhancedCache> enhancedCacheCache = new ConcurrentHashMap<>(1024);

    private final javax.cache.annotation.CacheResolver cacheResolver;

    @Override
    public Collection<? extends EnhancedCache> resolveCaches(CacheOperationInvocationContext<?> context) {
        if (!(context instanceof CacheInvocationContext<?>)) {
            throw new IllegalStateException("Unexpected context " + context);
        }
        CacheInvocationContext<?> cacheInvocationContext = (CacheInvocationContext<?>) context;
        Cache<Object, Object> cache = cacheResolver.resolveCache(cacheInvocationContext);
        if (cache == null) {
            throw new IllegalStateException("Could not resolve cache for " + context + " using " + this.cacheResolver);
        }
        return Collections.singleton(convert(cache));
    }

    private EnhancedCache convert(Cache<Object, Object> cache) {
        return enhancedCacheCache.computeIfAbsent(cache, EnhancedJCacheCache::new);
    }

}
