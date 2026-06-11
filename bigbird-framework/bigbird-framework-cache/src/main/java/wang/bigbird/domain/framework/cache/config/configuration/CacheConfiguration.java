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
package wang.bigbird.domain.framework.cache.config.configuration;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import wang.bigbird.domain.framework.cache.config.property.CacheProperties;
import wang.bigbird.domain.framework.cache.support.SpelResolvingCacheResolver;
import wang.bigbird.domain.framework.cache.support.caffeine.CustomizedCaffeineCacheManager;
import wang.bigbird.domain.framework.cache.support.condition.CacheEnableCondition;
import wang.bigbird.domain.framework.cache.support.redission.CustomizedRedissonCacheManager;

import javax.annotation.PostConstruct;

/**
 * 缓存配置
 *
 * @author Bigbird
 */
@Slf4j
@Configuration
@EnableCaching
@ComponentScan("wang.bigbird.domain.framework.cache")
@Conditional(CacheEnableCondition.class)
public class CacheConfiguration extends CachingConfigurerSupport {

    @Autowired
    private CacheManager cacheManager;

    @PostConstruct
    public void init() {
        log.info("Init cache framework.");
    }

    /**
     * 如果需要使用 redis 作为缓存中间件
     * 需要引入 bigbird-framework-data-redis，或者自己注册 redissonClient
     */
    @Bean
    @ConditionalOnProperty(prefix = "spring.cache", name = "type", havingValue = "redis")
    public CustomizedRedissonCacheManager redissonCacheManager(RedissonClient redissonClient, CacheProperties cacheProperties) {
        return new CustomizedRedissonCacheManager(redissonClient, cacheProperties);
    }

    /**
     * 使用 Caffeine 作为缓存中间件
     */
    @Bean
    @ConditionalOnProperty(prefix = "spring.cache", name = "type", havingValue = "caffeine")
    public CustomizedCaffeineCacheManager caffeineCacheManager(CacheProperties cacheProperties) {
        return new CustomizedCaffeineCacheManager(cacheProperties);
    }

    @Bean("spelResolvingCacheResolver")
    @Override
    public CacheResolver cacheResolver() {
        return new SpelResolvingCacheResolver(cacheManager);
    }

}
