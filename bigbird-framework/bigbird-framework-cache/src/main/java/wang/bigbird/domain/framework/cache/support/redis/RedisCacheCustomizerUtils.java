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
package wang.bigbird.domain.framework.cache.support.redis;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * @author Bigbird
 */
public abstract class RedisCacheCustomizerUtils {

    private static final Field DEFAULT_CACHE_CONFIGURATION_FIELD;

    static {
        DEFAULT_CACHE_CONFIGURATION_FIELD = ReflectionUtils.findField(RedisCacheManager.RedisCacheManagerBuilder.class,
                "defaultCacheConfiguration", RedisCacheConfiguration.class);
        assert DEFAULT_CACHE_CONFIGURATION_FIELD != null;
        ReflectionUtils.makeAccessible(DEFAULT_CACHE_CONFIGURATION_FIELD);
    }

    public static RedisCacheConfiguration getDefaultCacheConfigurationFor(RedisCacheManager.RedisCacheManagerBuilder builder) {
        return (RedisCacheConfiguration) ReflectionUtils.getField(DEFAULT_CACHE_CONFIGURATION_FIELD, builder);
    }

}
