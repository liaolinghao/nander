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

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheOperationInvoker;
import org.springframework.cache.jcache.interceptor.JCacheInterceptor;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.annotation.CacheAsMulti;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.interceptor.CacheAsMultiOperationInvoker;

import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheResult;
import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * AOP方法拦截器{@link JCacheInterceptor}的增强，在原本的拦截处理里增加了{@link CacheAsMulti @CacheAsMulti}注解的处理
 *
 * @author Bigbird
 */
public class EnhancedJCacheInterceptor extends JCacheInterceptor {

    @Nullable
    private CacheResultAsMultiInterceptor cacheResultMultiInterceptor;

    @Nullable
    private CachePutAsMultiInterceptor cachePutMultiInterceptor;

    @Nullable
    private CacheRemoveAsMultiInterceptor cacheRemoveMultiInterceptor;

    public EnhancedJCacheInterceptor(@Nullable Supplier<CacheErrorHandler> errorHandler) {
        super(errorHandler);
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        this.cacheResultMultiInterceptor = new CacheResultAsMultiInterceptor(getErrorHandler());
        this.cachePutMultiInterceptor = new CachePutAsMultiInterceptor(getErrorHandler());
        this.cacheRemoveMultiInterceptor = new CacheRemoveAsMultiInterceptor(getErrorHandler());
    }

    @Override
    @Nullable
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        Assert.state(invocation instanceof ProxyMethodInvocation,
                "Invocation must be ProxyMethodInvocation");
        CacheAsMultiOperationInvoker invoker = new CacheAsMultiOperationInvoker((ProxyMethodInvocation) invocation);
        Method method = invocation.getMethod();
        Object target = invocation.getThis();
        Assert.state(target != null, "Target must not be null");
        try {
            return execute(invoker, target, method, invocation.getArguments());
        } catch (CacheOperationInvoker.ThrowableWrapper th) {
            throw th.getOriginal();
        }
    }

    @Override
    @Nullable
    protected Object execute(CacheOperationInvoker invoker, Object target, Method method, Object[] args) {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
        AbstractJCacheAsMultiOperation<?> multiOperation = ((EnhancedJCacheOperationSource) getCacheOperationSource())
                .getCacheAsMultiOperation(method, targetClass);
        if (multiOperation != null) {
            CacheAsMultiOperationContext<?, ?> context =
                    new CacheAsMultiOperationContext<>(multiOperation, target, args);
            return execute(context, invoker);
        }
        return super.execute(invoker, target, method, args);
    }

    @Nullable
    private Object execute(CacheAsMultiOperationContext<?, ?> context, CacheOperationInvoker invoker) {
        AbstractJCacheAsMultiOperation<?> multiOperation = context.getMultiOperation();
        if (multiOperation instanceof CacheResultAsMultiOperation) {
            Assert.notNull(this.cacheResultMultiInterceptor, "No CacheResultMultiInterceptor");
            return this.cacheResultMultiInterceptor.invoke(
                    (CacheAsMultiOperationContext<CacheResultAsMultiOperation, CacheResult>) context, (CacheAsMultiOperationInvoker) invoker);
        } else if (multiOperation instanceof CachePutAsMultiOperation) {
            Assert.notNull(this.cachePutMultiInterceptor, "No CachePutMultiInterceptor");
            return this.cachePutMultiInterceptor.invoke(
                    (CacheAsMultiOperationContext<CachePutAsMultiOperation, CachePut>) context, invoker);
        } else if (multiOperation instanceof CacheRemoveAsMultiOperation) {
            Assert.notNull(this.cacheRemoveMultiInterceptor, "No CacheRemoveMultiInterceptor");
            return this.cacheRemoveMultiInterceptor.invoke(
                    (CacheAsMultiOperationContext<CacheRemoveAsMultiOperation, CacheRemove>) context, invoker);
        }
        return null;
    }

}
