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
package wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.convert.converter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.cache.caffeine.CaffeineCache;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.EnhancedCache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bigbird
 */
public class CaffeineEnhancedCacheConverter implements EnhancedCacheConverter<CaffeineCache> {

    @SneakyThrows
    @Override
    public EnhancedCache convert(CaffeineCache source) {
        return new CaffeineEnhancedCache(source.getName(), source.getNativeCache(), source.isAllowNullValues());
    }

    public static class CaffeineEnhancedCache extends CaffeineCache implements EnhancedCache {

        public CaffeineEnhancedCache(String name, Cache<Object, Object> cache, boolean allowNullValues) {
            super(name, cache, allowNullValues);
        }

        @Override
        public Map<Object, ValueWrapper> multiGet(Collection<?> keys) {
            Cache<Object, Object> cache = getNativeCache();
            Map<@NonNull Object, @NonNull Object> map;
            if (cache instanceof LoadingCache) {
                map = ((LoadingCache<Object, Object>) cache).getAll(keys);
            } else {
                map = cache.getAllPresent(keys);
            }
            Map<Object, ValueWrapper> newMap = new HashMap(keys.size());
            map.forEach((key, value) -> newMap.put(key, toValueWrapper(value)));
            return newMap;
        }

        @Override
        public void multiPut(Map<?, ?> map) {
            Map<Object, Object> newMap = new HashMap(map.size());
            map.forEach((key, value) -> newMap.put(key, toStoreValue(value)));
            getNativeCache().putAll(newMap);
        }

        @Override
        public void multiEvict(Collection<?> keys) {
            getNativeCache().invalidateAll(keys);
        }

    }
}
