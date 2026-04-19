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
package wang.bigbird.domain.framework.data.kafka.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * kafka消费者属性
 *
 * @author Bigbird
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "bigbird.data.kafka.consumer")
public class KafkaConsumerProperties {

    /**
     * kafka连接地址，多个地址用,分开
     */
    private String addresses;
    /**
     * 消费者的消费记录offset是否后台自动提交
     */
    private Boolean enableAutoCommit;
    /**
     * 当消费者的消费记录offset后台自动提交时，多长时间自动提交一次，单位毫秒
     */
    private Integer autoCommitInterval;
    /**
     * 当Kafka中没有初始偏移量或服务器上不再存在当前偏移量时的处理策略
     * earliest：自动将偏移量重置为最早的偏移量；
     * latest：自动将偏移量重置为最迟的偏移量；
     * none：如果未找到消费者组的先前偏移量，则将异常抛出给消费者；
     * exception：向消费者抛出异常；
     */
    private String autoOffsetReset;
    /**
     * 一次调用poll返回的最大记录数
     */
    private Integer maxPollRecords;
    /**
     * 消费者拉取消息时的最长等待时间（单位：毫秒）
     */
    private Integer fetchMaxWait;
    /**
     * 返回消息给消费者的最小字节数阈值（以字节为单位）
     */
    private Integer fetchMinSize;
    /**
     * 消费者协调员之间的心跳频率（单位是毫秒）
     */
    private Integer heartbeatInterval;

}
