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

import lombok.SneakyThrows;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.core.serializer.support.SerializationDelegate;
import org.springframework.lang.Nullable;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.EnhancedCache;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Bigbird
 */
public class ConcurrentMapEnhancedCacheConverter implements EnhancedCacheConverter<ConcurrentMapCache> {

    private static final Field SERIALIZATION_FIELD;

    static {
        try {
            SERIALIZATION_FIELD = ConcurrentMapCache.class.getDeclaredField("serialization");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        SERIALIZATION_FIELD.setAccessible(true);
    }

    @SneakyThrows
    @Override
    public EnhancedCache convert(ConcurrentMapCache source) {
        SerializationDelegate serialization;
        if (source.isStoreByValue()) {
            serialization = (SerializationDelegate) SERIALIZATION_FIELD.get(source);
        } else {
            serialization = null;
        }
        return new ConcurrentMapEnhancedCache(source.getName(), source.getNativeCache(),
                source.isAllowNullValues(), serialization);
    }

    public static class ConcurrentMapEnhancedCache extends ConcurrentMapCache implements EnhancedCache {

        protected ConcurrentMapEnhancedCache(String name, ConcurrentMap<Object, Object> store,
                                             boolean allowNullValues, @Nullable SerializationDelegate serialization) {
            super(name, store, allowNullValues, serialization);
        }

        @Override
        public Map<Object, ValueWrapper> multiGet(Collection<?> keys) {
            Map<Object, ValueWrapper> map = new HashMap(keys.size());
            keys.forEach(key -> {
                ValueWrapper valueWrapper = this.get(key);
                map.put(key, valueWrapper);
            });
            return map;
        }

        @Override
        @SneakyThrows
        public void multiPut(Map<?, ?> map) {
            Map<Object, Object> newMap = new HashMap(map.size());
            map.forEach((k, v) -> newMap.put(k, toStoreValue(v)));
            ConcurrentMap<Object, Object> store = getNativeCache();
            store.putAll(map);
        }

        @Override
        public void multiEvict(Collection<?> keys) {
            keys.forEach(this::evict);
        }

    }

}
