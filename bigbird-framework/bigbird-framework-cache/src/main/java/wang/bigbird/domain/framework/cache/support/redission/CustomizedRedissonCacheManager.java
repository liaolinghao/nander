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
import org.springframework.cache.Cache;
import wang.bigbird.domain.framework.cache.config.property.CacheProperties;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.spring.cache.CacheConfig;
import wang.bigbird.domain.framework.cache.support.CustomizedCacheManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
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

    private boolean dynamic;
    private boolean allowNullValues;
    private Codec codec;
    private RedissonClient redisson;
    private Map<String, CacheConfig> configMap;
    private ConcurrentMap<String, Cache> instanceMap;

    private CacheProperties.Redis redisCacheProperties;

    public CustomizedRedissonCacheManager(RedissonClient redisson, CacheProperties cacheProperties, Codec codec) {
        this.dynamic = true;
        this.allowNullValues = true;
        this.configMap = new ConcurrentHashMap();
        this.instanceMap = new ConcurrentHashMap();
        this.redisson = redisson;
        this.redisCacheProperties = cacheProperties.getRedis();
        this.codec = codec;
    }

    public void setAllowNullValues(boolean allowNullValues) {
        this.allowNullValues = allowNullValues;
    }

    public void setCacheNames(Collection<String> names) {
        if (names != null) {
            Iterator var2 = names.iterator();

            while (var2.hasNext()) {
                String name = (String) var2.next();
                this.getCache(name);
            }

            this.dynamic = false;
        } else {
            this.dynamic = true;
        }

    }

    public void setConfig(Map<String, CacheConfig> config) {
        this.configMap = config;
    }

    public void setRedisson(RedissonClient redisson) {
        this.redisson = redisson;
    }

    public void setCodec(Codec codec) {
        this.codec = codec;
    }

    protected CacheConfig createDefaultConfig() {
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setTTL(redisCacheProperties.getTimeToLive().toMillis());
        cacheConfig.setMaxIdleTime(redisCacheProperties.getMaxIdleTime().toMillis());
        return cacheConfig;
    }

    @Override
    public Cache getCache(String name) {
        Cache cache = this.instanceMap.get(name);
        if (cache != null) {
            return cache;
        } else if (!this.dynamic) {
            return cache;
        } else {
            CacheConfig config = this.configMap.get(name);
            if (config == null) {
                config = this.createDefaultConfig();
                config.setTTL(parseTTL(name,config.getTTL()));
                config.setMaxIdleTime(parseMaxIdleTime(name,config.getMaxIdleTime()));
                this.configMap.put(name, config);
            }
            return config.getMaxIdleTime() == 0L && config.getTTL() == 0L && config.getMaxSize() == 0 ? this.createMap(name, config) : this.createMapCache(name, config);
        }
    }

    /**
     * 无失效时间的缓存
     *
     * @param name
     * @param config
     * @return
     */
    private Cache createMap(String name, CacheConfig config) {
        RMap<Object, Object> map = this.getMap(name, config);
        Cache cache = new CustomizedRedissonCache(map, this.allowNullValues);
        Cache oldCache = this.instanceMap.putIfAbsent(name, cache);
        if (oldCache != null) {
            cache = oldCache;
        }

        return cache;
    }

    protected RMap<Object, Object> getMap(String name, CacheConfig config) {
        return this.codec != null ? this.redisson.getMap(name, this.codec) : this.redisson.getMap(name);
    }

    /**
     * 带失效时间的缓存
     *
     * @param name
     * @param config
     * @return
     */
    private Cache createMapCache(String name, CacheConfig config) {
        RMapCache<Object, Object> map = this.getMapCache(name, config);
        Cache cache = new CustomizedRedissonCache(map, config, this.allowNullValues);
        Cache oldCache = this.instanceMap.putIfAbsent(name, cache);
        if (oldCache != null) {
            cache = oldCache;
        } else {
            map.setMaxSize(config.getMaxSize());
        }
        return cache;
    }

    protected RMapCache<Object, Object> getMapCache(String name, CacheConfig config) {
        return this.codec != null ? this.redisson.getMapCache(name, this.codec) : this.redisson.getMapCache(name);
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(this.configMap.keySet());
    }

}
