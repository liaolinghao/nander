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
package wang.bigbird.domain.framework.common.logging.cloud.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import wang.bigbird.domain.framework.common.logging.core.base.enums.LogLevelEnum;

import java.util.List;

/**
 * Feign Logging properties
 *
 * @author Bigbird
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "bigbird.common.logging.feign")
public class FeignLoggingProperties {

    /**
     * 是否开启
     */
    private Boolean enable = false;
    /**
     * 级别，支持 trace debug info warn error
     */
    private LogLevelEnum level = LogLevelEnum.INFO;
    /**
     * 排除的类或方法
     */
    private List<String> excludes;
    /**
     * 序列化长度
     */
    private Integer serializeLength;
}
