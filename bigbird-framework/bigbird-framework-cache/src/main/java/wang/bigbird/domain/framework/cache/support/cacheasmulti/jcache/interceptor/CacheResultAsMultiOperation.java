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
package wang.bigbird.domain.framework.cache.support.cacheasmulti.jcache.interceptor;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.jcache.interceptor.JCacheOperation;
import org.springframework.lang.Nullable;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.annotation.CacheAsMultiParameterDetail;

import javax.cache.annotation.CacheResult;

/**
 * @author Bigbird
 */
@Slf4j
class CacheResultAsMultiOperation extends AbstractJCacheAsMultiOperation<CacheResult> {

    @Nullable
    @Getter
    private final CacheResolver exceptionCacheResolver;

    @SneakyThrows
    public CacheResultAsMultiOperation(JCacheOperation<CacheResult> operation, KeyGenerator keyGenerator,
                                       CacheAsMultiParameterDetail parameterDetail, @Nullable CacheResolver exceptionCacheResolver) {
        super(operation, keyGenerator, parameterDetail);
        this.exceptionCacheResolver = exceptionCacheResolver;
        if (returnTypeMaker == null) {
            throw new IllegalStateException("The returnType must not be null on " + operation.getMethod().getName());
        }
    }

    public boolean isAlwaysInvoked() {
        return operation.getCacheAnnotation().skipGet();
    }

}
