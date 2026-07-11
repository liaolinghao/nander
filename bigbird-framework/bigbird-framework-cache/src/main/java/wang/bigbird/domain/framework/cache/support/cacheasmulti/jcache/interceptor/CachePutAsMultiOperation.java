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

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheValue;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Bigbird
 */
@Slf4j
class CachePutAsMultiOperation extends AbstractJCacheAsMultiOperation<CachePut> {

    @Getter
    private final ExceptionTypeFilter exceptionTypeFilter;

    @SneakyThrows
    public CachePutAsMultiOperation(JCacheOperation<CachePut> operation,
                                    KeyGenerator keyGenerator, CacheAsMultiParameterDetail parameterDetail) {
        super(operation, keyGenerator, parameterDetail);
        CachePut ann = operation.getCacheAnnotation();
        exceptionTypeFilter = initializeExceptionTypeFilter(ann.cacheFor(), ann.noCacheFor());
        // @CacheValue注解参数默认不会用来做key，所以需要将它放进去
        initializeCachePutOperationKeyParameterDetails(operation, this.parameterDetail);
    }

    @Override
    protected void validateParameterDetail(Method method, CacheAsMultiParameterDetail parameterDetail) {
        // 如果方法被@CachePut注解，那@CacheAsMulti注解必须在@CacheValue注解的参数上
        if (!parameterDetail.isAnnotationPresent(CacheValue.class)) {
            throw new IllegalStateException("The @CacheAsMulti parameter should be same as @CacheValue on " + method);
        }
        Class<?> rawType = parameterDetail.getRawType();
        // 参数必须是Map类型
        if (!Map.class.isAssignableFrom(rawType)) {
            throw new IllegalStateException("The @CacheAsMulti parameter should be a map on " + method);
        }
    }

    public boolean isEarlyPut() {
        return !operation.getCacheAnnotation().afterInvocation();
    }

    private static void initializeCachePutOperationKeyParameterDetails(
            JCacheOperation<?> operation, CacheAsMultiParameterDetail parameterDetail) {
        // 如果@CacheAsMulti参数上有@CacheKey，直接返回
        if (parameterDetail.isAnnotationPresent(CacheKey.class)) {
            return;
        }
        // 此时所有参数均为@CacheKey
        List<Object> keyParameterList = (List<Object>) getCacheOperationField(operation, "keyParameterDetails");
        Objects.requireNonNull(keyParameterList);
        List<Object> allParameterList = (List<Object>) getCacheOperationField(operation, "allParameterDetails");
        Objects.requireNonNull(allParameterList);
        keyParameterList.clear();
        keyParameterList.addAll(allParameterList);
    }

}
