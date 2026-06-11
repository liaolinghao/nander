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
 * 原因为RedissonCache对于过期时间设置无效，该类仅在put方法处增加了使过期时间生效的设置
 *
 * @author Bigbird
 */
public class CustomizedRedissonCache implements Cache {

    private RMapCache<Object, Object> mapCache;
    private final RMap<Object, Object> map;
    private CacheConfig config;
    private final boolean allowNullValues;
    private final AtomicLong hits;
    private final AtomicLong puts;
    private final AtomicLong misses;

    public CustomizedRedissonCache(RMapCache<Object, Object> mapCache, CacheConfig config, boolean allowNullValues) {
        this(mapCache, allowNullValues);
        this.mapCache = mapCache;
        this.config = config;
    }

    public CustomizedRedissonCache(RMap<Object, Object> map, boolean allowNullValues) {
        this.hits = new AtomicLong();
        this.puts = new AtomicLong();
        this.misses = new AtomicLong();
        this.map = map;
        this.allowNullValues = allowNullValues;
    }

    @Override
    public String getName() {
        return this.map.getName();
    }

    @Override
    public RMap<?, ?> getNativeCache() {
        return this.map;
    }

    @Override
    public ValueWrapper get(Object key) {
        Object value;
        if (this.mapCache != null && this.config.getMaxIdleTime() == 0L && this.config.getMaxSize() == 0) {
            value = this.mapCache.getWithTTLOnly(key);
        } else {
            value = this.map.get(key);
        }
        if (value == null) {
            this.addCacheMiss();
        } else {
            this.addCacheHit();
        }
        return this.toValueWrapper(value);
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        Object value;
        if (this.mapCache != null && this.config.getMaxIdleTime() == 0L && this.config.getMaxSize() == 0) {
            value = this.mapCache.getWithTTLOnly(key);
        } else {
            value = this.map.get(key);
        }
        if (value == null) {
            this.addCacheMiss();
        } else {
            this.addCacheHit();
            if (value.getClass().getName().equals(NullValue.class.getName())) {
                return null;
            }

            if (type != null && !type.isInstance(value)) {
                throw new IllegalStateException("Cached value is not of required type [" + type.getName() + "]: " + value);
            }
        }
        return (T) this.fromStoreValue(value);
    }

    @Override
    public void put(Object key, Object value) {
        if (!this.allowNullValues && value == null) {
            this.map.remove(key);
        } else {
            value = this.toStoreValue(value);
            if (this.mapCache != null) {
                this.mapCache.fastPut(key, value, this.config.getTTL(), TimeUnit.MILLISECONDS, this.config.getMaxIdleTime(), TimeUnit.MILLISECONDS);
                // 需要进行以下设置，过期时间才能生效
                this.mapCache.expire(config.getTTL(), TimeUnit.MILLISECONDS);
            } else {
                this.map.fastPut(key, value);
            }
            this.addCachePut();
        }
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        Object prevValue;
        if (!this.allowNullValues && value == null) {
            prevValue = this.map.get(key);
        } else {
            value = this.toStoreValue(value);
            if (this.mapCache != null) {
                prevValue = this.mapCache.putIfAbsent(key, value, this.config.getTTL(), TimeUnit.MILLISECONDS, this.config.getMaxIdleTime(), TimeUnit.MILLISECONDS);
                // 需要进行以下设置，过期时间才能生效
                this.mapCache.expire(config.getTTL(), TimeUnit.MILLISECONDS);
            } else {
                prevValue = this.map.putIfAbsent(key, value);
            }
            if (prevValue == null) {
                this.addCachePut();
            }
        }
        return this.toValueWrapper(prevValue);
    }

    @Override
    public void evict(Object key) {
        this.map.fastRemove(new Object[]{key});
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    private ValueWrapper toValueWrapper(Object value) {
        if (value == null) {
            return null;
        } else {
            return (ValueWrapper) (value.getClass().getName().equals(NullValue.class.getName()) ? NullValue.INSTANCE : new SimpleValueWrapper(value));
        }
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object value;
        if (this.mapCache != null && this.config.getMaxIdleTime() == 0L && this.config.getMaxSize() == 0) {
            value = this.mapCache.getWithTTLOnly(key);
        } else {
            value = this.map.get(key);
        }
        if (value == null) {
            this.addCacheMiss();
            RLock lock = this.map.getLock(key);
            lock.lock();
            try {
                value = this.map.get(key);
                if (value == null) {
                    value = this.putValue(key, valueLoader, value);
                }
            } finally {
                lock.unlock();
            }
        } else {
            this.addCacheHit();
        }
        return (T) this.fromStoreValue(value);
    }

    private <T> Object putValue(Object key, Callable<T> valueLoader, Object value) {
        try {
            value = valueLoader.call();
        } catch (Exception var9) {
            Exception ex = var9;
            RuntimeException exception;
            try {
                Class<?> c = Class.forName("org.springframework.cache.Cache$ValueRetrievalException");
                Constructor<?> constructor = c.getConstructor(Object.class, Callable.class, Throwable.class);
                exception = (RuntimeException) constructor.newInstance(key, valueLoader, ex);
            } catch (Exception var8) {
                throw new IllegalStateException(var8);
            }
            throw exception;
        }
        this.put(key, value);
        return value;
    }

    protected Object fromStoreValue(Object storeValue) {
        return storeValue instanceof NullValue ? null : storeValue;
    }

    protected Object toStoreValue(Object userValue) {
        return userValue == null ? NullValue.INSTANCE : userValue;
    }

    public long getCacheHits() {
        return this.hits.get();
    }

    public long getCacheMisses() {
        return this.misses.get();
    }

    public long getCachePuts() {
        return this.puts.get();
    }

    private void addCachePut() {
        this.puts.incrementAndGet();
    }

    private void addCacheHit() {
        this.hits.incrementAndGet();
    }

    private void addCacheMiss() {
        this.misses.incrementAndGet();
    }

}

