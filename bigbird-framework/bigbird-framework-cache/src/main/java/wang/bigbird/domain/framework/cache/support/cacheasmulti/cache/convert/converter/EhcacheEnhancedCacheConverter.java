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
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.lang.Nullable;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.EnhancedCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bigbird
 */
public class EhcacheEnhancedCacheConverter implements EnhancedCacheConverter<EhCacheCache> {

    @SneakyThrows
    @Override
    public EnhancedCache convert(EhCacheCache source) {
        return new EhcacheEnhancedCache(source.getNativeCache());
    }

    public static class EhcacheEnhancedCache extends EhCacheCache implements EnhancedCache {

        public EhcacheEnhancedCache(Ehcache ehcache) {
            super(ehcache);
        }

        @Override
        public Map<Object, ValueWrapper> multiGet(Collection<?> keys) {
            Map<Object, Element> map = getNativeCache().getAll(keys);
            Map<Object, ValueWrapper> newMap = new HashMap(keys.size());
            map.forEach((key, value) -> newMap.put(key, toValueWrapper(value)));
            return newMap;
        }

        @Override
        public void multiPut(Map<?, ?> map) {
            Collection<Element> elements = new ArrayList<>(map.size());
            map.forEach((key, value) -> elements.add(new Element(key, value)));
            getNativeCache().putAll(elements);
        }

        @Override
        public void multiEvict(Collection<?> keys) {
            getNativeCache().removeAll(keys);
        }

        @Nullable
        private ValueWrapper toValueWrapper(@Nullable Element element) {
            return (element != null ? new SimpleValueWrapper(element.getObjectValue()) : null);
        }

    }
}
