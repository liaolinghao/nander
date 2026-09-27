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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.redisson.api.*;
import org.redisson.spring.cache.CacheConfig;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.EnhancedCache;
import wang.bigbird.domain.framework.cache.support.redission.CustomizedRedissonCache;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * {@link CustomizedRedissonCache} 的批量操作转换器，
 * 避免 {@code @CacheAsMulti} 走兜底适配器 {@code EnhancedCacheAdapter} 逐个 key 往返 Redis
 *
 * <p>批量语义与单 key 方法严格对齐：
 * <ul>
 *     <li>{@code multiGet}：{@code getAll} 一次往返；
 *     RMapCache 形态覆写的 {@code getAll} 会顺带维护 idle 追踪结构，
 *     与单 key 读的 {@code getWithTTLOnly} 相比多一次簿记写，但无 TTL 语义影响</li>
 *     <li>{@code multiPut}：RMapCache 形态不能用 {@code fastPutAll}（3.17.x 不支持 entry 级 TTL），
 *     改为 {@code fastPutAsync} 携带 TTL 参数逐 entry 异步提交后统一 {@code await()}，
 *     命令在一条连接上连续发出，总耗时约一次往返；{@code allowNullValues=false} 时 null 值
 *     与单 key {@code put} 一致：删除旧缓存而非跳过。纯 RMap 形态走 {@code putAll} 一次往返</li>
 *     <li>{@code multiEvict}：{@code fastRemove(Object...)} 一次往返</li>
 * </ul>
 *
 * @author Bigbird
 */
public class CustomizedRedissonEnhancedCacheConverter implements EnhancedCacheConverter<CustomizedRedissonCache> {

    @Override
    public EnhancedCache convert(CustomizedRedissonCache source) {
        RMapCache<Object, Object> mapCache = source.getMapCache();
        if (mapCache != null) {
            return new CustomizedRedissonEnhancedCache(mapCache, source.getConfig(), source.isAllowNullValues(), source.getRedissonClient());
        }
        return new CustomizedRedissonEnhancedCache(source.getNativeCache(), source.isAllowNullValues(), source.getRedissonClient());
    }

    static class CustomizedRedissonEnhancedCache extends CustomizedRedissonCache implements EnhancedCache {

        private final RedissonClient redissonClient;

        CustomizedRedissonEnhancedCache(RMapCache<Object, Object> mapCache, CacheConfig config, boolean allowNullValues, RedissonClient redissonClient) {
            super(mapCache, config, allowNullValues, redissonClient);
            this.redissonClient = redissonClient;
        }

        CustomizedRedissonEnhancedCache(RMap<?, ?> map, boolean allowNullValues, RedissonClient redissonClient) {
            super((RMap<Object, Object>) map, allowNullValues, redissonClient);
            this.redissonClient = redissonClient;
        }

        @Override
        public Map<Object, ValueWrapper> multiGet(Collection<?> keys) {
            if (keys.isEmpty()) {
                return Collections.emptyMap();
            }
            // getAll 参数是 Set；HashSet 去重，重复 key 的返回值不受影响（同一个映射值）
            Set<Object> uniqueKeys = Sets.newHashSet(keys);
            Map<Object, Object> values = getNativeCache().getAll(uniqueKeys);
            Map<Object, ValueWrapper> result = Maps.newLinkedHashMapWithExpectedSize(uniqueKeys.size());
            for (Object key : keys) {
                if (result.containsKey(key)) {
                    // 重复 key，已统计过
                    continue;
                }
                Object value = values.get(key);
                if (value != null) {
                    addCacheHit();
                } else {
                    addCacheMiss();
                }
                result.put(key, toValueWrapper(value));
            }
            return result;
        }

        @Override
        public void multiPut(Map<?, ?> map) {
            if (map.isEmpty()) {
                return;
            }
            if (getMapCache() != null) {
                putAllToMapCache(map);
            } else {
                putAllToMap(map);
            }
        }

        @Override
        public void multiEvict(Collection<?> keys) {
            if (keys.isEmpty()) {
                return;
            }
            getNativeCache().fastRemove(keys.toArray());
        }

        /**
         * RMapCache 形态不能用 fastPutAll：3.17.x 的 fastPutAll 不携带 entry 级 TTL 参数，
         * 批量写入的 entry 将永不过期。改为逐 entry fastPutAsync 后统一 await，
         * 命令在一条连接上连续发出，总耗时约一次往返
         */
        private void putAllToMapCache(Map<?, ?> map) {
            RMapCache<Object, Object> mapCache = getMapCache();
            CacheConfig config = getConfig();
            if (config == null) {
                // 正常不会走到：mapCache 形态只能由携带 CacheConfig 的构造器创建
                throw new IllegalStateException("RMapCache instance must be created with CacheConfig");
            }
            RBatch batch = redissonClient.createBatch(BatchOptions.defaults().executionMode(BatchOptions.ExecutionMode.IN_MEMORY));
            RMapCacheAsync<Object, Object> asyncMap = batch.getMapCache(getName(), mapCache.getCodec());
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getValue() == null && !isAllowNullValues()) {
                    // 与单 key put 语义一致：不缓存 null，同时删除已有旧值
                    // 在 RBatch 上下文里，此处可以逐条删除，不需要收集起来再一次性删除
                    asyncMap.fastRemoveAsync(entry.getKey());
                    continue;
                }
                asyncMap.fastPutAsync(entry.getKey(), toStoreValue(entry.getValue()),
                        config.getTTL(), TimeUnit.MILLISECONDS,
                        config.getMaxIdleTime(), TimeUnit.MILLISECONDS);
                addCachePut();
            }
            // 一次往返执行全部命令
            batch.execute();
        }

        /**
         * 纯 RMap 形态无 entry 级 TTL 语义，直接 putAll 一次往返；
         * allowNullValues=false 时 null 值与单 key put 一致：先删旧值再批量写入
         */
        @SuppressWarnings("unchecked")
        private void putAllToMap(Map<?, ?> map) {
            RMap<Object, Object> nativeCache = getNativeCache();
            Map<Object, Object> toPut = Maps.newHashMapWithExpectedSize(map.size());
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getValue() == null && !isAllowNullValues()) {
                    nativeCache.remove(entry.getKey());
                    continue;
                }
                toPut.put(entry.getKey(), toStoreValue(entry.getValue()));
                addCachePut();
            }
            if (!toPut.isEmpty()) {
                nativeCache.putAll(toPut);
            }
        }

    }

}
