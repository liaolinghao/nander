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
package wang.bigbird.domain.framework.cache.support.cacheasmulti.jcache;

import org.springframework.cache.jcache.JCacheCache;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.EnhancedCache;

import javax.cache.Cache;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * 对JSR-107的{@link Cache}进行包装，适配为{@link EnhancedCache}
 *
 * @author Bigbird
 */
public class EnhancedJCacheCache extends JCacheCache implements EnhancedCache {

    public EnhancedJCacheCache(Cache<Object, Object> jcache) {
        super(jcache);
    }

    public EnhancedJCacheCache(Cache<Object, Object> jcache, boolean allowNullValues) {
        super(jcache, allowNullValues);
    }

    @Override
    public Map<Object, ValueWrapper> multiGet(Collection<?> keys) {
        Map<Object, Object> map = getNativeCache().getAll(new HashSet<>(keys));
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
        getNativeCache().removeAll(new HashSet<>(keys));
    }

}
