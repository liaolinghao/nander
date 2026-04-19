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
package wang.bigbird.domain.framework.data.rabbitmq.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * rabbitmq 属性
 *
 * @author Bigbird
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "bigbird.data.rabbitmq")
public class RabbitmqProperties {

    /**
     * 加解密密钥
     */
    private String key = "bigbird";
    /**
     * 节点地址, 逗号分隔
     */
    private String addresses;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 虚拟主机
     */
    private String virtualHost;

    /**
     * receive() 操作的超时时间
     */
    private Long receiveTimeout;

    /**
     * sendAndReceive() 操作的超时时间
     */
    private Long replyTimeout;

    /**
     * 最小的消费者数量
     */
    private Integer concurrentConsumers;

    /**
     * 最大的消费者数量
     */
    private Integer maxConcurrentConsumers;

}
