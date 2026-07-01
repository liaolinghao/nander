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
package wang.bigbird.domain.framework.common.logging.core.support.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.common.logging.core.base.enums.LogLevelEnum;
import wang.bigbird.domain.framework.common.logging.core.base.util.AopTargetUtils;
import wang.bigbird.domain.framework.common.logging.core.config.property.CustomLoggingProperties;
import wang.bigbird.domain.framework.common.logging.core.domain.pojo.ExcludeWrapper;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;

/**
 * 自定义日志 Aop
 *
 * @author Bigbird
 */
@Aspect
@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "bigbird.common.logging.custom",
        name = "enable",
        havingValue = "true"
)
public class CustomLoggingAop extends BaseLoggingAop implements Ordered {

    @Autowired
    private CustomLoggingProperties customLoggingProperties;

    @Override
    @Pointcut("@annotation(wang.bigbird.domain.framework.common.logging.core.support.annotation.Logging)")
    public void pointcut() {
        // do nothing
    }

    @Override
    public String getClassSimpleName(ProceedingJoinPoint joinPoint) throws Exception {
        Object target = AopTargetUtils.getTarget(joinPoint.getTarget());
        Class<?> clazz = target.getClass();
        return clazz.getSimpleName();
    }

    @Override
    public String getLoggingType() {
        return "CST";
    }

    @Override
    public LogLevelEnum getLoggingLevel() {
        return customLoggingProperties.getLevel();
    }

    @Override
    public ExcludeWrapper getExclude() {
        return null;
    }

    @Override
    public Integer getSerializeLength() {
        return customLoggingProperties.getSerializeLength();
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return JsonUtils.getMapper();
    }

    @Override
    protected boolean canSerialize(Object o) {
        return !(o instanceof byte[]);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
