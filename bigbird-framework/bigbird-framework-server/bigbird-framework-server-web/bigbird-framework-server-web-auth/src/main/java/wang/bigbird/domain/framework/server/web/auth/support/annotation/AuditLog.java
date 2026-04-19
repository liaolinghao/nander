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
package wang.bigbird.domain.framework.server.web.auth.support.annotation;

import java.lang.annotation.*;

/**
 * 审计日志注解
 *
 * @author Bigbird
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * 操作平台名称
     *
     * @return
     */
    String platform() default "";

    /**
     * 操作模块名称
     *
     * @return
     */
    String module() default "";

    /**
     * 操作行为描述
     *
     * @return
     */
    String description() default "";

    /**
     * 日志存储模式，0-日志文件，1-关系型数据库，2-文档型数据库
     *
     * @return
     */
    byte mode() default 0;

}
