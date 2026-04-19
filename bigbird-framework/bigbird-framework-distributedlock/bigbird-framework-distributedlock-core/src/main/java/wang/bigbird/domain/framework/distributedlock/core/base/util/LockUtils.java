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
package wang.bigbird.domain.framework.distributedlock.core.base.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;

/**
 * 锁工具类
 *
 * @author Bigbird
 */
public class LockUtils {

    private LockUtils() {
        throw new IllegalStateException();
    }

    /**
     * 通过spel表达式获取key
     *
     * 在spel表达式中，默认情况下，表达式前缀为#，而后缀为}
     * 比如：#{systemProperties['user.language']}，将会转换为用户语言系统的属性
     * 表达式也可采用#msg，将会采用context中的msg值进行代替
     *
     * @param lockKeyPrefix 分布式锁键前缀
     * @param lockKey 分布式锁键值
     * @param parameterNames
     * @param values
     * @return
     */
    public static String getKeyBySpel(String lockKeyPrefix, String lockKey, String[] parameterNames, Object[] values) {
        if (StringUtils.isBlank(lockKey)) {
            return null;
        }
        if (StringUtils.isBlank(lockKeyPrefix)) {
            lockKeyPrefix = "";
        }
        if (!lockKey.contains(CommonConstants.POUND_SIGN)) {
            return lockKeyPrefix + lockKey;
        }
        ExpressionParser parser = new SpelExpressionParser();
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], values[i]);
        }
        Expression expression = parser.parseExpression(lockKey);
        Object value = expression.getValue(context);
        if (null == value) {
            return null;
        }
        return lockKeyPrefix + value;
    }
}
