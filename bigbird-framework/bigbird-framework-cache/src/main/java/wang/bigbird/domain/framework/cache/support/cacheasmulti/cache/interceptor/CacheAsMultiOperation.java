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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.CacheEvictOperation;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CachePutOperation;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.ast.CompoundExpression;
import org.springframework.expression.spel.ast.Indexer;
import org.springframework.expression.spel.ast.Literal;
import org.springframework.expression.spel.ast.PropertyOrFieldReference;
import org.springframework.expression.spel.ast.VariableReference;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.annotation.CacheAsMulti;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.annotation.CacheAsMultiParameterDetail;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 被{@link  CacheAsMulti @CacheAsMulti}注解的方法进行解析的结果，
 * 缓存在{@link EnhancedCachingOperationSource}中
 *
 * @author Bigbird
 */
@Slf4j
class CacheAsMultiOperation<O extends CacheOperation> extends AbstractCacheAsMultiOperation {

    /**
     * java8 编译未配置-parameters参数时，获取到的参数名是arg0，所以用这个类
     */
    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    @Getter
    protected final O operation;

    public CacheAsMultiOperation(Method method, O operation, CacheAsMultiParameterDetail parameterDetail) {
        super(method, parameterDetail);
        this.operation = operation;
        validate(method, operation, parameterDetail);
        if (returnTypeMaker == null && !(operation instanceof CacheEvictOperation)) {
            throw new IllegalStateException("The returnType must not be null when operation is not instanceof CacheEvictOperation on " + method);
        }
    }

    private static void validate(
            Method method, CacheOperation operation, CacheAsMultiParameterDetail parameterDetail) {
        if (StringUtils.hasText(operation.getKey())) {
            KeyExpressionParser keyExpressionParser = new KeyExpressionParser(operation.getKey());
            String[] parameterNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
            Objects.requireNonNull(parameterNames);
            int parameterPosition = parameterDetail.getPosition();
            boolean parameterInExpression = keyExpressionParser.containParameter(parameterPosition)
                    || keyExpressionParser.containParameter(parameterNames[parameterPosition]);
            if (!parameterInExpression) {
                if (operation instanceof CachePutOperation || operation instanceof CacheEvictOperation) {
                    parameterInExpression = keyExpressionParser.containParameter("result");
                }
            }
            if (!parameterInExpression) {
                throw new IllegalStateException("The @CacheAsMulti parameter or result should be in key expression on " + method);
            }
        }
    }

    protected static class KeyExpressionParser {

        private final Set<Integer> indexSet = new HashSet<>();

        private final Set<String> nameSet = new HashSet<>();

        private static final SpelExpressionParser PARSER = new SpelExpressionParser();

        private static final Pattern PATTERN_ARG_INDEX = Pattern.compile("^#(?:a|p)(\\d+)$");

        public static final String ROOT_VARIABLE = "#root";

        public static final String ARGS_VARIABLE = "args";

        public KeyExpressionParser(String keyExpression) {
            SpelExpression expression = (SpelExpression) PARSER.parseExpression(keyExpression);
            SpelNode ast = expression.getAST();
            scanNode(ast);
        }

        public boolean containParameter(String name) {
            return nameSet.contains(name);
        }

        public boolean containParameter(int position) {
            return indexSet.contains(position);
        }

        private void scanNode(SpelNode node) {
            if (node instanceof CompoundExpression) {
                SpelNode child0 = node.getChild(0);
                if (!(child0 instanceof VariableReference)) {
                    return;
                }
                String child0NodeName = child0.toStringAST();
                if (!ROOT_VARIABLE.equals(child0NodeName)) {
                    scanNode(child0);
                    return;
                }
                SpelNode child1 = node.getChild(1);
                if (!(child1 instanceof PropertyOrFieldReference) || !ARGS_VARIABLE.equals(child1.toStringAST())) {
                    return;
                }
                SpelNode child2 = node.getChild(2);
                if (!(child2 instanceof Indexer)) {
                    return;
                }
                Literal indexNode = (Literal) child2.getChild(0);
                String originalValue = indexNode.getOriginalValue();
                Objects.requireNonNull(originalValue);
                indexSet.add(Integer.valueOf(originalValue));
            } else if (node instanceof VariableReference) {
                String nodeName = node.toStringAST();
                if (ROOT_VARIABLE.equals(nodeName)) {
                    return;
                }
                Matcher m = PATTERN_ARG_INDEX.matcher(nodeName);
                if (m.matches()) {
                    indexSet.add(Integer.valueOf(m.group(1)));
                } else {
                    nameSet.add(nodeName.substring(1));
                }
            } else {
                int childCount = node.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    scanNode(node.getChild(i));
                }
            }
        }
    }

}
