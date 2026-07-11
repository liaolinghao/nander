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
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.jcache.interceptor.JCacheOperation;
import org.springframework.util.ExceptionTypeFilter;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.annotation.CacheAsMultiParameterDetail;

import javax.cache.annotation.CacheRemove;

/**
 * @author Bigbird
 */
@Slf4j
class CacheRemoveAsMultiOperation extends AbstractJCacheAsMultiOperation<CacheRemove> {

    @Getter
    private final ExceptionTypeFilter exceptionTypeFilter;

    @SneakyThrows
    public CacheRemoveAsMultiOperation(JCacheOperation<CacheRemove> operation,
                                       KeyGenerator keyGenerator, CacheAsMultiParameterDetail parameterDetail) {
        super(operation, keyGenerator, parameterDetail);
        CacheRemove ann = operation.getCacheAnnotation();
        exceptionTypeFilter = initializeExceptionTypeFilter(ann.evictFor(), ann.noEvictFor());
    }

    public boolean isEarlyRemove() {
        return !operation.getCacheAnnotation().afterInvocation();
    }

}
