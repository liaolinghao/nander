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
package wang.bigbird.domain.framework.server.web.core.support.annotation;

import wang.bigbird.domain.framework.server.web.core.support.validator.SensitiveWordValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 敏感词校验注解
 *
 * @author Bigbird
 */
@Documented
@Constraint(validatedBy = SensitiveWordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SensitiveWord {

    /**
     * 默认提示语
     *
     * @return
     */
    String message() default "内容包含敏感词，请修改后重试";

    /**
     * 校验分组（JSR标准）
     *
     * @return
     */
    Class<?>[] groups() default {};

    /**
     * 负载（JSR标准）
     *
     * @return
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * 是否开启校验（可动态开关）
     *
     * @return
     */
    boolean enable() default true;

    /**
     * 是否关闭默认错误提示信息（可动态开关）
     *
     * @return
     */
    boolean disableDefaultMessage() default false;

}
