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
package wang.bigbird.domain.framework.cache.support;

import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * cacheName spel 解析
 * <p>
 * 将缓存名称表达式中的参数采用具体的参数值替换后获得最后的缓存名称
 * 该解析器将在调用缓存方法时，截取缓存设置中的cacheNames，将表达式进行转换
 *
 * @author Bigbird
 */
public class SpelResolvingCacheResolver extends SimpleCacheResolver {


    public SpelResolvingCacheResolver(CacheManager cacheManager) {
        super(cacheManager);
    }

    /**
     * 将缓存设置中的cacheNames根据表达式进行转换，将真实的参数值替换表达式中的参数项
     * 获得最终的缓存名称
     *
     * @param context
     * @return 替换参数值后的缓存名称
     */
    @Override
    protected Collection<String> getCacheNames(CacheOperationInvocationContext<?> context) {
        Collection<String> unresolvedCacheNames = super.getCacheNames(context);
        return unresolvedCacheNames.stream()
                .map(unresolvedCacheName -> {
                    if (!unresolvedCacheName.contains(CommonConstants.POUND_SIGN)) {
                        return unresolvedCacheName;
                    }
                    ExpressionParser parser = new SpelExpressionParser();
                    EvaluationContext evaluationContext = new StandardEvaluationContext();
                    String[] parameterNames = new LocalVariableTableParameterNameDiscoverer().getParameterNames(context.getMethod());
                    Object[] args = context.getArgs();
                    for (int i = 0; i < parameterNames.length; i++) {
                        evaluationContext.setVariable(parameterNames[i], args[i]);
                    }
                    Expression expression = parser.parseExpression(unresolvedCacheName);
                    Object value = expression.getValue(evaluationContext);
                    return value.toString();
                }).collect(Collectors.toList());
    }

}
