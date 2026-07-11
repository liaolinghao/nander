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
package wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.cache.interceptor.CacheOperationInvoker;
import org.springframework.lang.Nullable;

/**
 * @author Bigbird
 */
@RequiredArgsConstructor
public class CacheAsMultiOperationInvoker implements CacheOperationInvoker {

    private final ProxyMethodInvocation invocation;

    @Nullable
    @Override
    public Object invoke() throws ThrowableWrapper {
        try {
            return invocation.proceed();
        } catch (Throwable ex) {
            throw new ThrowableWrapper(ex);
        }
    }

    @Nullable
    public Object invoke(Object[] args) throws ThrowableWrapper {
        try {
            return invocation.invocableClone(args).proceed();
        } catch (Throwable ex) {
            throw new ThrowableWrapper(ex);
        }
    }

}
