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
package wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.AbstractCachingConfiguration;
import org.springframework.cache.annotation.ProxyCachingConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.convert.EnhancedCacheConversionService;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.convert.converter.EnhancedCacheConverter;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.jcache.config.EnhancedJCacheConfiguration;
import wang.bigbird.domain.framework.cache.support.generator.TypeMethodKeyGenerator;

import java.util.Collection;

/**
 * @author Bigbird
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(ProxyCachingConfiguration.class)
public class EnhancedCacheAutoConfiguration extends AbstractCachingConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public EnhancedCachingConfiguration enhancedCachingConfiguration() {
        return new EnhancedCachingConfiguration();
    }

    @Bean
    @ConditionalOnBean(type = "org.springframework.cache.jcache.config.ProxyJCacheConfiguration")
    @ConditionalOnMissingBean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public EnhancedJCacheConfiguration enhancedJCacheConfiguration() {
        return new EnhancedJCacheConfiguration();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean
    public static EnhancedCachePostProcessor enhancedCachingPostProcessor() {
        return new EnhancedCachePostProcessor();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean
    public EnhancedCacheConversionService enhancedCacheConversionService(Collection<EnhancedCacheConverter<?>> converters) {
        return new EnhancedCacheConversionService(converters);
    }

    @Bean
    @ConditionalOnMissingBean
    public TypeMethodKeyGenerator typeMethodKeyGenerator() {
        return new TypeMethodKeyGenerator();
    }

}
