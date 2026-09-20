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
package wang.bigbird.domain.framework.cache.support.redission;

import lombok.Data;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.NullValue;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.lang.reflect.Constructor;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 该类基本参照RedissonCache原样拷贝过来，
 * 原因：为支持缓存批量操作，需要将其中字段的可见范围进行修正
 *
 * @author Bigbird
 */
@Data
public class CustomizedRedissonCache implements Cache {

    /**
     * 带单元素 TTL、最大空闲、淘汰策略的 Map
     */
    private RMapCache<Object, Object> mapCache;
    /**
     * 基础 Redis Map，**整个 Hash 无单个 field 过期能力**
     */
    private final RMap<Object, Object> map;
    /**
     * 缓存配置：TTL、maxIdleTime、maxSize，空值是否允许
     */
    private CacheConfig config;
    /**
     * 是否允许缓存 null（Redisson 用`NullValue.INSTANCE`占位存储 null）
     */
    private final boolean allowNullValues;
    /**
     * 命中统计计数器
     */
    private final AtomicLong hits = new AtomicLong();
    /**
     * 写入统计计数器
     */
    private final AtomicLong puts = new AtomicLong();
    /**
     * 未命中统计计数器
     */
    private final AtomicLong misses = new AtomicLong();

    public CustomizedRedissonCache(RMapCache<Object, Object> mapCache, CacheConfig config, boolean allowNullValues) {
        this(mapCache, allowNullValues);
        this.mapCache = mapCache;
        this.config = config;
    }

    public CustomizedRedissonCache(RMap<Object, Object> map, boolean allowNullValues) {
        this.map = map;
        this.allowNullValues = allowNullValues;
    }

    @Override
    public String getName() {
        return map.getName();
    }

    @Override
    public RMap<Object, Object> getNativeCache() {
        return map;
    }

    @Override
    public ValueWrapper get(Object key) {
        Object value;
        if (mapCache != null && config.getMaxIdleTime() == 0 && config.getMaxSize() == 0) {
            // config.getMaxIdleTime() == 0 → 没有配置 idle 过期，不需要维护 idle ZSET
            // config.getMaxSize() == 0 → 没有配置 maxSize 淘汰，不需要淘汰簿记
            // 只查 TTL 过期 ZSET，命中则返回	不更新 idle 追踪 ZSET，不写入
            // 走 getWithTTLOnly 省掉一次 Redis 写操作
            value = mapCache.getWithTTLOnly(key);
        } else {
            // 需要 idle 追踪/maxSize 淘汰，必须每次读都刷新 idle 时间戳
            value = map.get(key);
        }
        if (value == null) {
            addCacheMiss();
        } else {
            addCacheHit();
        }
        return toValueWrapper(value);
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        Object value;
        if (mapCache != null && config.getMaxIdleTime() == 0 && config.getMaxSize() == 0) {
            // config.getMaxIdleTime() == 0 → 没有配置 idle 过期，不需要维护 idle ZSET
            // config.getMaxSize() == 0 → 没有配置 maxSize 淘汰，不需要淘汰簿记
            // 只查 TTL 过期 ZSET，命中则返回	不更新 idle 追踪 ZSET，不写入
            // 走 getWithTTLOnly 省掉一次 Redis 写操作
            value = mapCache.getWithTTLOnly(key);
        } else {
            // 需要 idle 追踪/maxSize 淘汰，必须每次读都刷新 idle 时间戳
            value = map.get(key);
        }
        if (value == null) {
            addCacheMiss();
        } else {
            addCacheHit();
            if (value.getClass().getName().equals(NullValue.class.getName())) {
                return null;
            }
            if (type != null && !type.isInstance(value)) {
                throw new IllegalStateException("Cached value is not of required type [" + type.getName() + "]: " + value);
            }
        }
        return (T) fromStoreValue(value);
    }

    @Override
    public void put(Object key, Object value) {
        if (!allowNullValues && value == null) {
            map.remove(key);
            return;
        }
        value = toStoreValue(value);
        if (mapCache != null) {
            mapCache.fastPut(key, value, config.getTTL(), TimeUnit.MILLISECONDS, config.getMaxIdleTime(), TimeUnit.MILLISECONDS);
        } else {
            map.fastPut(key, value);
        }
        addCachePut();
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        Object prevValue;
        if (!allowNullValues && value == null) {
            prevValue = map.get(key);
        } else {
            value = toStoreValue(value);
            if (mapCache != null) {
                prevValue = mapCache.putIfAbsent(key, value, config.getTTL(), TimeUnit.MILLISECONDS, config.getMaxIdleTime(), TimeUnit.MILLISECONDS);
            } else {
                prevValue = map.putIfAbsent(key, value);
            }
            if (prevValue == null) {
                addCachePut();
            }
        }
        return toValueWrapper(prevValue);
    }

    @Override
    public void evict(Object key) {
        map.fastRemove(key);
    }

    @Override
    public void clear() {
        map.clear();
    }

    protected ValueWrapper toValueWrapper(Object value) {
        if (value == null) {
            return null;
        }
        if (value.getClass().getName().equals(NullValue.class.getName())) {
            return NullValue.INSTANCE;
        }
        return new SimpleValueWrapper(value);
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object value;
        if (mapCache != null && config.getMaxIdleTime() == 0 && config.getMaxSize() == 0) {
            // config.getMaxIdleTime() == 0 → 没有配置 idle 过期，不需要维护 idle ZSET
            // config.getMaxSize() == 0 → 没有配置 maxSize 淘汰，不需要淘汰簿记
            // 只查 TTL 过期 ZSET，命中则返回	不更新 idle 追踪 ZSET，不写入
            // 走 getWithTTLOnly 省掉一次 Redis 写操作
            value = mapCache.getWithTTLOnly(key);
        } else {
            // 需要 idle 追踪/maxSize 淘汰，必须每次读都刷新 idle 时间戳
            value = map.get(key);
        }
        if (value == null) {
            addCacheMiss();
            RLock lock = map.getLock(key);
            lock.lock();
            try {
                value = map.get(key);
                if (value == null) {
                    value = putValue(key, valueLoader, value);
                }
            } finally {
                lock.unlock();
            }
        } else {
            addCacheHit();
        }
        return (T) fromStoreValue(value);
    }

    private <T> Object putValue(Object key, Callable<T> valueLoader, Object value) {
        try {
            value = valueLoader.call();
        } catch (Exception ex) {
            RuntimeException exception;
            try {
                Class<?> c = Class.forName("org.springframework.cache.Cache$ValueRetrievalException");
                Constructor<?> constructor = c.getConstructor(Object.class, Callable.class, Throwable.class);
                exception = (RuntimeException) constructor.newInstance(key, valueLoader, ex);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            throw exception;
        }
        put(key, value);
        return value;
    }

    protected Object fromStoreValue(Object storeValue) {
        if (storeValue instanceof NullValue) {
            return null;
        }
        return storeValue;
    }

    protected Object toStoreValue(Object userValue) {
        if (userValue == null) {
            return NullValue.INSTANCE;
        }
        return userValue;
    }

    /**
     * The number of get requests that were satisfied by the cache.
     *
     * @return the number of hits
     */
    protected long getCacheHits() {
        return hits.get();
    }

    /**
     * A miss is a get request that is not satisfied.
     *
     * @return the number of misses
     */
    protected long getCacheMisses() {
        return misses.get();
    }

    protected long getCachePuts() {
        return puts.get();
    }

    protected void addCachePut() {
        puts.incrementAndGet();
    }

    protected void addCacheHit() {
        hits.incrementAndGet();
    }

    protected void addCacheMiss() {
        misses.incrementAndGet();
    }

}

