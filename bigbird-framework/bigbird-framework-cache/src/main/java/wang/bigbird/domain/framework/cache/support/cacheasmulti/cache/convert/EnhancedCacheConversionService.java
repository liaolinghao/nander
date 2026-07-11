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
package wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.convert;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurationSelector;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.EnhancedCache;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.convert.converter.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 缓存转换服务，将Spring的{@link Cache}转成{@link EnhancedCache}
 *
 * @author Bigbird
 */
@Slf4j
public class EnhancedCacheConversionService extends GenericConversionService implements EnhancedCacheConverter<Cache> {

    private static final String REDIS_CACHE_CLASS_NAME = "org.springframework.data.redis.cache.RedisCache";
    private static final String CONCURRENT_MAP_CACHE_CLASS_NAME = "org.springframework.cache.concurrent.ConcurrentMapCache";
    private static final String EHCACHE_CLASS_NAME = "net.sf.ehcache.Ehcache";
    private static final String CAFFEINE_CACHE_CLASS_NAME = "com.github.benmanes.caffeine.cache.Cache";

    public EnhancedCacheConversionService(Collection<EnhancedCacheConverter<?>> converters) {
        ClassLoader classLoader = CachingConfigurationSelector.class.getClassLoader();
        if (ClassUtils.isPresent(REDIS_CACHE_CLASS_NAME, classLoader)) {
            addConverter(new RedisEnhancedCacheConverter());
        }
        if (ClassUtils.isPresent(CONCURRENT_MAP_CACHE_CLASS_NAME, classLoader)) {
            addConverter(new ConcurrentMapEnhancedCacheConverter());
        }
        if (ClassUtils.isPresent(EHCACHE_CLASS_NAME, classLoader)) {
            addConverter(new EhcacheEnhancedCacheConverter());
        }
        if (ClassUtils.isPresent(CAFFEINE_CACHE_CLASS_NAME, classLoader)) {
            addConverter(new CaffeineEnhancedCacheConverter());
        }
        converters.forEach(this::addConverter);
        if (log.isDebugEnabled()) {
            log.debug("Add EnhancedCacheConverter: [{}]", converters.stream().map(c -> c.getClass().getSimpleName()).collect(Collectors.joining(", ")));
        }
    }

    @Override
    @NonNull
    public EnhancedCache convert(Cache source) {
        if (super.canConvert(source.getClass(), EnhancedCache.class)) {
            return Objects.requireNonNull(super.convert(source, EnhancedCache.class));
        } else {
            log.warn("Cache {} is converted by EnhancedCacheAdapter, it is recommended to declare a dedicated EnhancedCache and EnhancedCacheConverter class", source.getClass().getSimpleName());
            return new EnhancedCacheAdapter(source);
        }
    }

    /**
     * 对Spring的{@link Cache}进行包装，变为{@link EnhancedCache}
     * 只是作为兜底，尽量不要直接使用
     *
     * @author zhumengshuai
     */
    @RequiredArgsConstructor
    static class EnhancedCacheAdapter implements EnhancedCache {

        @Delegate
        private final Cache cache;

        @Override
        public Map<Object, ValueWrapper> multiGet(Collection<?> keys) {
            HashMap<Object, ValueWrapper> map = new HashMap(keys.size());
            keys.forEach(k -> {
                ValueWrapper valueWrapper = cache.get(k);
                map.put(k, valueWrapper);
            });
            return map;
        }

        @Override
        public void multiPut(Map<?, ?> map) {
            map.forEach(cache::put);
        }

        @Override
        public void multiEvict(Collection<?> keys) {
            keys.forEach(cache::evict);
        }

    }

}
