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

import javax.cache.annotation.CacheRemove;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Bigbird
 */
@Slf4j
class CacheRemoveAsMultiInterceptor extends AbstractJCacheAsMultiInterceptor<CacheRemoveAsMultiOperation, CacheRemove> {

    public CacheRemoveAsMultiInterceptor(CacheErrorHandler errorHandler) {
        super(errorHandler);
    }

    @Override
    @SneakyThrows
    public Object invoke(CacheAsMultiOperationContext<CacheRemoveAsMultiOperation, CacheRemove> context, CacheOperationInvoker invoker) {
        CacheRemoveAsMultiOperation multiOperation = context.getMultiOperation();
        boolean earlyRemove = multiOperation.isEarlyRemove();
        if (earlyRemove) {
            removeValues(context);
        }
        try {
            Object result = invoker.invoke();
            if (!earlyRemove) {
                removeValues(context);
            }
            return result;
        } catch (CacheOperationInvoker.ThrowableWrapper wrapperException) {
            Throwable ex = wrapperException.getOriginal();
            if (!earlyRemove && multiOperation.getExceptionTypeFilter().match(ex.getClass())) {
                removeValues(context);
            }
            throw wrapperException;
        }
    }

    private void removeValues(CacheAsMultiOperationContext<CacheRemoveAsMultiOperation, CacheRemove> context) {
        // 如果@CacheAsMulti注解的参数值为null或者空集合，则调用原方法返回
        Collection<?> cacheAsMultiArg = (Collection<?>) context.getCacheAsMultiArg();
        if (CollectionUtils.isEmpty(cacheAsMultiArg)) {
            return;
        }
        Collection<Object> keys = new ArrayList<>(cacheAsMultiArg.size());
        cacheAsMultiArg.forEach(argItem -> keys.add(context.generateKey(argItem)));
        EnhancedCache cache = resolveCache(context);
        if (log.isTraceEnabled()) {
            log.trace("Evict keys " + keys + " for operation " + context.getOperation());
        }
        doMultiEvict(cache, keys);
    }

}

