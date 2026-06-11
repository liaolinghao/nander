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

import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.spring.cache.CacheConfig;
import org.springframework.cache.Cache;
import wang.bigbird.domain.framework.cache.config.property.CacheProperties;
import wang.bigbird.domain.framework.cache.support.CustomizedCacheManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于Redisson的缓存管理器，
 * 可实现根据缓存名称调整缓存过期时间
 *
 * @author Bigbird
 */
public class CustomizedRedissonCacheManager extends CustomizedCacheManager {

    private boolean dynamic = true;
    private boolean allowNullValues = true;
    private Codec codec = CustomizedJsonJacksonCodec.INSTANCE;
    private RedissonClient redisson;
    private Map<String, CacheConfig> configMap = new ConcurrentHashMap();
    private ConcurrentMap<String, Cache> instanceMap = new ConcurrentHashMap();

    private CacheProperties.Redis redisCacheProperties;

    public CustomizedRedissonCacheManager(RedissonClient redisson, CacheProperties config) {
        this.redisson = redisson;
        this.redisCacheProperties = config.getRedis();
    }

    protected CacheConfig createDefaultConfig() {
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setTTL(redisCacheProperties.getTimeToLive().toMillis());
        cacheConfig.setMaxIdleTime(redisCacheProperties.getMaxIdleTime().toMillis());
        return cacheConfig;
    }

    @Override
    public Cache getCache(String name) {
        Cache cache = instanceMap.get(name);
        if (cache != null) {
            return cache;
        } else if (!dynamic) {
            return null;
        } else {
            CacheConfig config = configMap.get(name);
            if (config == null) {
                config = createDefaultConfig();
                config.setTTL(parseTTL(name, config.getTTL()));
                config.setMaxIdleTime(parseMaxIdleTime(name, config.getMaxIdleTime()));
                configMap.put(name, config);
            }
            return config.getMaxIdleTime() == 0L && config.getTTL() == 0L && config.getMaxSize() == 0 ? createMap(name) : createMapCache(name, config);
        }
    }

    /**
     * 无失效时间的缓存
     *
     * @param name
     * @return
     */
    private Cache createMap(String name) {
        RMap<Object, Object> map = getMap(name);
        Cache cache = new CustomizedRedissonCache(map, allowNullValues);
        Cache oldCache = instanceMap.putIfAbsent(name, cache);
        if (oldCache != null) {
            cache = oldCache;
        }
        return cache;
    }

    private RMap<Object, Object> getMap(String name) {
        return redisson.getMap(name, codec);
    }

    /**
     * 带失效时间的缓存
     *
     * @param name
     * @param config
     * @return
     */
    private Cache createMapCache(String name, CacheConfig config) {
        RMapCache<Object, Object> map = getMapCache(name);
        Cache cache = new CustomizedRedissonCache(map, config, allowNullValues);
        Cache oldCache = instanceMap.putIfAbsent(name, cache);
        if (oldCache != null) {
            cache = oldCache;
        } else {
            map.setMaxSize(config.getMaxSize());
        }
        return cache;
    }

    private RMapCache<Object, Object> getMapCache(String name) {
        return redisson.getMapCache(name, codec);
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(configMap.keySet());
    }

}
