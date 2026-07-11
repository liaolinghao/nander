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

import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.jcache.interceptor.DefaultJCacheOperationSource;
import org.springframework.cache.jcache.interceptor.JCacheOperation;
import org.springframework.core.MethodClassKey;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.util.function.SingletonSupplier;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.annotation.CacheAsMultiAnnotationUtils;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.annotation.CacheAsMultiParameterDetail;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.convert.EnhancedCacheConversionService;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.interceptor.EnhancedCachingResolverAdapter;

import javax.cache.annotation.CacheDefaults;
import javax.cache.annotation.CacheMethodDetails;
import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheResolverFactory;
import javax.cache.annotation.CacheResult;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author Bigbird
 */
public class EnhancedJCacheOperationSource extends DefaultJCacheOperationSource {

    private final Map<Object, AbstractJCacheAsMultiOperation<?>> cacheAsMultiOperationCache = new ConcurrentHashMap<>(1024);

    private final SingletonSupplier<CacheResolver> adaptedCacheResolver =
            SingletonSupplier.of(() -> new EnhancedCachingResolverAdapter(
                    super.getDefaultCacheResolver(), getBean(EnhancedCacheConversionService.class)));

    private final SingletonSupplier<CacheResolver> adaptedExceptionCacheResolver =
            SingletonSupplier.of(() -> new EnhancedCachingResolverAdapter(
                    super.getDefaultExceptionCacheResolver(), getBean(EnhancedCacheConversionService.class)));

    public EnhancedJCacheOperationSource(@Nullable Supplier<CacheManager> cacheManager, @Nullable Supplier<CacheResolver> cacheResolver,
                                         @Nullable Supplier<CacheResolver> exceptionCacheResolver, @Nullable Supplier<KeyGenerator> keyGenerator) {
        super(cacheManager, cacheResolver, exceptionCacheResolver, keyGenerator);
    }

    @Nullable
    public AbstractJCacheAsMultiOperation<?> getCacheAsMultiOperation(Method method, @Nullable Class<?> targetClass) {
        Object cacheKey = getCacheKey(method, targetClass);
        return cacheAsMultiOperationCache.get(cacheKey);
    }

    @Override
    protected JCacheOperation<?> findCacheOperation(Method method, @Nullable Class<?> targetClass) {
        JCacheOperation<?> cacheOperation = super.findCacheOperation(method, targetClass);
        if (cacheOperation == null) {
            return null;
        }
        CacheAsMultiParameterDetail parameterDetail = CacheAsMultiAnnotationUtils.findAnnotation(method);
        if (parameterDetail == null) {
            return cacheOperation;
        }
        CacheDefaults defaults = getCacheDefaults(method, targetClass);
        AbstractJCacheAsMultiOperation<?> cacheAsMultiOperation = parseCacheAsMultiAnnotation(defaults, cacheOperation, parameterDetail);
        if (cacheAsMultiOperation != null) {
            Object cacheKey = getCacheKey(method, targetClass);
            cacheAsMultiOperationCache.put(cacheKey, cacheAsMultiOperation);
        }
        // 用CacheOperation来对外表现，这样就可以用Spring自身的KeyGeneratorAdapter来生成Key。
        return cacheOperation;
    }

    @Nullable
    private AbstractJCacheAsMultiOperation<?> parseCacheAsMultiAnnotation(
            @Nullable CacheDefaults defaults, JCacheOperation<?> cacheOperation, CacheAsMultiParameterDetail parameterDetail) {
        Annotation cacheAnnotation = cacheOperation.getCacheAnnotation();
        if (cacheAnnotation instanceof CacheResult) {
            return createCacheResultMultiOperation(defaults, (JCacheOperation<CacheResult>) cacheOperation, parameterDetail);
        } else if (cacheAnnotation instanceof CachePut) {
            return createCachePutMultiOperation(defaults, (JCacheOperation<CachePut>) cacheOperation, parameterDetail);
        } else if (cacheAnnotation instanceof CacheRemove) {
            return createCacheRemoveMultiOperation(defaults, (JCacheOperation<CacheRemove>) cacheOperation, parameterDetail);
        } else {
            return null;
        }
    }

    private Object getCacheKey(Method method, @Nullable Class<?> targetClass) {
        return new MethodClassKey(method, targetClass);
    }

    protected CacheResultAsMultiOperation createCacheResultMultiOperation(
            @Nullable CacheDefaults defaults, JCacheOperation<CacheResult> cacheOperation, CacheAsMultiParameterDetail parameterDetail) {
        CacheResult ann = cacheOperation.getCacheAnnotation();
        KeyGenerator keyGenerator = determineKeyGenerator(defaults, ann.cacheKeyGenerator());
        CacheResolverFactory cacheResolverFactory =
                determineCacheResolverFactory(defaults, ann.cacheResolverFactory());
        CacheResolver exceptionCacheResolver = null;
        final String exceptionCacheName = ann.exceptionCacheName();
        if (StringUtils.hasText(exceptionCacheName)) {
            exceptionCacheResolver = getExceptionCacheResolver(cacheResolverFactory, cacheOperation);
        }
        return new CacheResultAsMultiOperation(cacheOperation, keyGenerator, parameterDetail, exceptionCacheResolver);
    }

    protected CachePutAsMultiOperation createCachePutMultiOperation(
            @Nullable CacheDefaults defaults, JCacheOperation<CachePut> cacheOperation, CacheAsMultiParameterDetail parameterDetail) {
        CachePut ann = cacheOperation.getCacheAnnotation();
        KeyGenerator keyGenerator = determineKeyGenerator(defaults, ann.cacheKeyGenerator());
        return new CachePutAsMultiOperation(cacheOperation, keyGenerator, parameterDetail);
    }

    protected CacheRemoveAsMultiOperation createCacheRemoveMultiOperation(
            @Nullable CacheDefaults defaults, JCacheOperation<CacheRemove> cacheOperation, CacheAsMultiParameterDetail parameterDetail) {
        CacheRemove ann = cacheOperation.getCacheAnnotation();
        KeyGenerator keyGenerator = determineKeyGenerator(defaults, ann.cacheKeyGenerator());
        return new CacheRemoveAsMultiOperation(cacheOperation, keyGenerator, parameterDetail);
    }

    @Override
    protected CacheResolver getCacheResolver(
            @Nullable CacheResolverFactory factory, CacheMethodDetails<?> details) {
        if (factory != null) {
            javax.cache.annotation.CacheResolver cacheResolver = factory.getCacheResolver(details);
            return new EnhancedJCacheResolverAdapter(cacheResolver);
        } else {
            return getDefaultCacheResolver();
        }
    }

    @Override
    protected CacheResolver getExceptionCacheResolver(
            @Nullable CacheResolverFactory factory, CacheMethodDetails<CacheResult> details) {
        if (factory != null) {
            javax.cache.annotation.CacheResolver cacheResolver = factory.getExceptionCacheResolver(details);
            return new EnhancedJCacheResolverAdapter(cacheResolver);
        } else {
            return getDefaultExceptionCacheResolver();
        }
    }

    @Override
    protected CacheResolver getDefaultCacheResolver() {
        return adaptedCacheResolver.obtain();
    }

    @Override
    protected CacheResolver getDefaultExceptionCacheResolver() {
        return adaptedExceptionCacheResolver.obtain();
    }

}
