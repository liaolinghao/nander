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

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheOperationInvoker;
import org.springframework.util.CollectionUtils;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.EnhancedCache;

import javax.cache.annotation.CachePut;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bigbird
 */
@Slf4j
class CachePutAsMultiInterceptor extends AbstractJCacheAsMultiInterceptor<CachePutAsMultiOperation, CachePut> {

    public CachePutAsMultiInterceptor(CacheErrorHandler errorHandler) {
        super(errorHandler);
    }

    @Override
    @SneakyThrows
    public Object invoke(CacheAsMultiOperationContext<CachePutAsMultiOperation, CachePut> context, CacheOperationInvoker invoker) {
        CachePutAsMultiOperation multiOperation = context.getMultiOperation();
        boolean earlyPut = multiOperation.isEarlyPut();
        if (earlyPut) {
            cacheValues(context);
        }
        try {
            Object result = invoker.invoke();
            if (!earlyPut) {
                cacheValues(context);
            }
            return result;
        } catch (CacheOperationInvoker.ThrowableWrapper ex) {
            Throwable original = ex.getOriginal();
            if (!earlyPut && multiOperation.getExceptionTypeFilter().match(original.getClass())) {
                cacheValues(context);
            }
            throw ex;
        }
    }

    protected void cacheValues(CacheAsMultiOperationContext<CachePutAsMultiOperation, CachePut> context) {
        // 如果@CacheAsMulti注解的参数值为null或者空集合，则调用原方法返回
        Map<?, ?> cacheAsMultiArg = (Map<?, ?>) context.getCacheAsMultiArg();
        if (CollectionUtils.isEmpty(cacheAsMultiArg)) {
            return;
        }
        Map<Object, Object> keyValueMap = new HashMap(cacheAsMultiArg.size());
        cacheAsMultiArg.forEach((argItem, value) -> keyValueMap.put(context.generateKey(argItem), value));
        EnhancedCache cache = resolveCache(context);
        if (log.isTraceEnabled()) {
            log.trace("Store key-value map " + keyValueMap + " for operation " + context.getOperation());
        }
        doMultiPut(cache, keyValueMap);
    }

}

