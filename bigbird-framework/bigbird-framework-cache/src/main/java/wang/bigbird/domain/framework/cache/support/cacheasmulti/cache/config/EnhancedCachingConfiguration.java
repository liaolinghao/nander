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

import org.springframework.cache.annotation.AbstractCachingConfiguration;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.cache.interceptor.CacheOperationSource;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.interceptor.EnhancedCachingInterceptor;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.interceptor.EnhancedCachingOperationSource;

/**
 * @author Bigbird
 */
public class EnhancedCachingConfiguration extends AbstractCachingConfiguration {

    /**
     * 通过{@link EnhancedCachePostProcessor}注册
     *
     * @return
     */
    public CacheOperationSource cacheOperationSource() {
        return new EnhancedCachingOperationSource();
    }

    /**
     * 通过{@link EnhancedCachePostProcessor}注册
     *
     * @param cacheOperationSource
     * @return
     */
    public CacheInterceptor cacheInterceptor(CacheOperationSource cacheOperationSource) {
        CacheInterceptor interceptor = new EnhancedCachingInterceptor();
        interceptor.configure(this.errorHandler, this.keyGenerator, this.cacheResolver, this.cacheManager);
        interceptor.setCacheOperationSource(cacheOperationSource);
        return interceptor;
    }

}
