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
package wang.bigbird.domain.framework.common.logging.cloud.support.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import wang.bigbird.domain.framework.common.logging.cloud.config.property.FeignLoggingProperties;
import wang.bigbird.domain.framework.common.logging.core.base.util.AopTargetUtils;
import wang.bigbird.domain.framework.common.logging.core.base.util.ExcludeHandlerUtils;
import wang.bigbird.domain.framework.common.logging.core.domain.pojo.ExcludeWrapper;
import wang.bigbird.domain.framework.common.logging.core.support.aop.BaseLoggingAop;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Feign 日志 Aop
 *
 * @author Bigbird
 */
@Aspect
@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "bigbird.common.logging.feign",
        name = "enable",
        havingValue = "true"
)
public class FeignLoggingAop extends BaseLoggingAop implements Ordered {

    @Autowired
    private FeignLoggingProperties feignLoggingProperties;

    @Override
    @Pointcut("(@annotation(org.springframework.web.bind.annotation.GetMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PostMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PutMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PatchMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.RequestMapping)) " +
            "&& @target(org.springframework.cloud.openfeign.FeignClient) ")
    public void pointcut() {
        // do nothing
    }

    @Override
    public String getClassSimpleName(ProceedingJoinPoint joinPoint) throws Exception {
        Object target = AopTargetUtils.getTarget(joinPoint.getTarget());
        Field h = target.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        // target
        Object t = h.get(target);
        Field targetField = t.getClass().getDeclaredField("target");
        targetField.setAccessible(true);
        // type
        Object type = targetField.get(t);
        Field typeField = type.getClass().getDeclaredField("type");
        typeField.setAccessible(true);
        Class clazz = (Class) typeField.get(type);
        return clazz.getSimpleName();
    }

    @Override
    public String getLoggingType() {
        return "RPC";
    }

    @Override
    public String getLoggingLevel() {
        return feignLoggingProperties.getLevel();
    }

    @Override
    public ExcludeWrapper getExclude() {
        List<String> excludes = feignLoggingProperties.getExcludes();
        return ExcludeHandlerUtils.buildExcludeWrapper(excludes);
    }

    @Override
    public Integer getSerializeLength() {
        return feignLoggingProperties.getSerializeLength();
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return JsonUtils.getMapper();
    }

    @Override
    protected boolean canSerialize(Object o) {
        return !(o instanceof HttpServletRequest)
                && !(o instanceof HttpServletResponse)
                && !(o instanceof MultipartFile)
                && !(o instanceof MultipartFile[])
                && !(o instanceof FilePart)
                && !(o instanceof FilePart[])
                && !(o instanceof byte[]);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
