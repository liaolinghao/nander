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
package wang.bigbird.domain.framework.server.web.core.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.server.web.core.base.enums.RejectedPolicyEnum;

/**
 * 线程池配置
 *
 * @author Bigbird
 */
@Data
@Component
@ConfigurationProperties(prefix = "bigbird.server.web.core.pool")
public class ThreadPoolProperties {

    /**
     * 最大线程数量
     */
    private Integer maxPoolSize = 50;

    /**
     * 核心线程数量
     */
    private Integer corePoolSize = 20;

    /**
     * 队列最大长度
     */
    private Integer queueCapacity = 100000;

    /**
     * 线程存活时间，秒为单位
     */
    private Integer keepAliveSeconds = 60;

    /**
     * 线程前缀
     */
    private String threadNamePrefix = "async-task-";

    /**
     * 拒绝策略
     */
    private RejectedPolicyEnum policy = RejectedPolicyEnum.ABORT;

}
