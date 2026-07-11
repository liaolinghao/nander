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
package wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.annotation;

import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @author Bigbird
 */
public abstract class CacheAsMultiAnnotationUtils {

    @Nullable
    public static CacheAsMultiParameterDetail findAnnotation(Method method) {
        CacheAsMultiParameterDetail detail = null;
        int parameterCount = method.getParameterCount();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameterCount; i++) {
            Parameter parameter = parameters[i];
            if (!parameter.isAnnotationPresent(CacheAsMulti.class)) {
                continue;
            }
            // @CacheAsMulti注解只能存在一个
            if (detail != null) {
                throw new IllegalStateException("There can be only one @CacheAsMulti annotation in method parameters on " + method);
            }
            detail = new CacheAsMultiParameterDetail(method, i);
        }
        return detail;
    }

}
