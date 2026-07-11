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
import org.springframework.util.ReflectionUtils;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.annotation.CacheAsMulti;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.annotation.CacheAsMultiParameterDetail;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.interceptor.AbstractCacheAsMultiOperation;

import javax.cache.annotation.CacheKey;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Objects;

/**
 * 被{@link CacheAsMulti @CacheAsMulti}注解的方法进行解析的结果，
 * 缓存在{@link EnhancedJCacheOperationSource}中
 *
 * @author Bigbird
 */
@Slf4j
abstract class AbstractJCacheAsMultiOperation<A extends Annotation> extends AbstractCacheAsMultiOperation {

    @Getter
    protected final JCacheOperation<A> operation;
    @Getter
    protected final KeyGenerator keyGenerator;

    public AbstractJCacheAsMultiOperation(JCacheOperation<A> operation,
                                          KeyGenerator keyGenerator, CacheAsMultiParameterDetail parameterDetail) {
        super(operation.getMethod(), parameterDetail);
        this.operation = operation;
        this.keyGenerator = keyGenerator;
        validate(operation, parameterDetail);
    }

    protected static ExceptionTypeFilter initializeExceptionTypeFilter(
            Class<? extends Throwable>[] includes, Class<? extends Throwable>[] excludes) {
        return new ExceptionTypeFilter(Arrays.asList(includes), Arrays.asList(excludes), true);
    }

    private static void validate(
            JCacheOperation<?> operation, CacheAsMultiParameterDetail parameterDetail) {
        Method method = operation.getMethod();
        // 如果@CacheAsMulti注解的参数没有@CacheKey注解，那其他参数也不能有@CacheKey注解
        if (!parameterDetail.isAnnotationPresent(CacheKey.class)) {
            for (Parameter parameter : method.getParameters()) {
                if (parameter.isAnnotationPresent(CacheKey.class)) {
                    throw new IllegalStateException("The @CacheAsMulti parameter should has @CacheKey when other parameter has @CacheKey on " + method);
                }
            }
        }
    }

    @SneakyThrows
    protected static Object getCacheOperationField(JCacheOperation<?> operation, String fieldName) {
        Class<?> operationClass = operation.getClass();
        Field field = ReflectionUtils.findField(operationClass, fieldName);
        Objects.requireNonNull(field, "Invalid operation, not found " + fieldName + " on class " + operation.getClass().getName());
        field.setAccessible(true);
        return field.get(operation);
    }

}
