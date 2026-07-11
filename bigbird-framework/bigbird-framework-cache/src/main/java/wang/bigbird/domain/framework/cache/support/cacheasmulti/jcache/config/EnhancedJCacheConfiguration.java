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
package wang.bigbird.domain.framework.cache.support.cacheasmulti.jcache.config;

import org.springframework.cache.jcache.config.AbstractJCacheConfiguration;
import org.springframework.cache.jcache.config.ProxyJCacheConfiguration;
import org.springframework.cache.jcache.interceptor.JCacheInterceptor;
import org.springframework.cache.jcache.interceptor.JCacheOperationSource;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.config.EnhancedCachePostProcessor;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.jcache.interceptor.EnhancedJCacheInterceptor;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.jcache.interceptor.EnhancedJCacheOperationSource;

/**
 * 定义新的Bean用来替换{@link ProxyJCacheConfiguration}中定义的
 *
 * @author Bigbird
 */
public class EnhancedJCacheConfiguration extends AbstractJCacheConfiguration {

    /**
     * 通过{@link EnhancedCachePostProcessor}根据条件注册
     *
     * @return
     */
    public JCacheOperationSource jCacheOperationSource() {
        return new EnhancedJCacheOperationSource(
                cacheManager, cacheResolver, exceptionCacheResolver, keyGenerator);
    }

    /**
     * 通过{@link EnhancedCachePostProcessor}根据条件注册
     *
     * @param jCacheOperationSource
     * @return
     */
    public JCacheInterceptor jCacheInterceptor(JCacheOperationSource jCacheOperationSource) {
        JCacheInterceptor interceptor = new EnhancedJCacheInterceptor(errorHandler);
        interceptor.setCacheOperationSource(jCacheOperationSource);
        return interceptor;
    }

}
