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

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.AbstractCachingConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.config.EnhancedCacheAutoConfiguration;

/**
 * @author Bigbird
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.springframework.data.redis.cache.RedisCacheManager")
@ConditionalOnBean(EnhancedCacheAutoConfiguration.class)
public class RedisCacheCustomizerAutoConfiguration extends AbstractCachingConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "spring.cache.redis.cache-as-multi", name = "serialize-to-json", havingValue = "true")
    public RedisCacheSerializeCustomizer redisCacheSerializeCustomizer() {
        return new RedisCacheSerializeCustomizer();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "spring.cache.redis.cache-as-multi")
    public RedisCacheTtlCustomizer redisCacheTtlCustomizer() {
        return new RedisCacheTtlCustomizer();
    }

}
