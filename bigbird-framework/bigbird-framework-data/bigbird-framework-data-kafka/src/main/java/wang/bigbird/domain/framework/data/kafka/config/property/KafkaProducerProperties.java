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
 * kafka生产者属性
 *
 * @author Bigbird
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "bigbird.data.kafka.producer")
public class KafkaProducerProperties {

    /**
     * kafka连接地址，多个地址用,分开
     */
    private String addresses;
    /**
     * ack标识，生产者要求数据有多少个副本接收到数据才算发送成功
     * "0"：表示生产者数据发送到leader就算写入成功，但是如果leader在把数据写到本地磁盘时报错，就会数据丢失，akcs设置为0时，kafka可以达到最大的吞吐量；
     * "1"：表示生产者数据发送到leader并写入到磁盘才算写入成功，但是如果数据在同步到其他副本时，leader挂了，其他副本被选举为新leader，那么就会有数据丢失；
     * "-1"，"all"：表示生产者把数据发送到leader，并同步到其他副本，才算数据写入成功，这种模式一般不会产生数据丢失，但是kafka的吞吐量会很低；
     */
    private String acks;
    /**
     * 默认批处理大小（以字节为单位）。
     * 小批量会降低吞吐量（零批量将完全禁用批处理）
     * 默认128kb
     */
    private Long batchSize;

    /**
     * 生产者可以用来缓冲等待发送到服务器的记录的内存总字节数
     * 默认64M
     */
    private Long bufferMemory;
    /**
     * 生产者生成的所有数据的压缩类型，默认值为"none"，可以配置为"gzip"，"snappy"和"lz4"
     */
    private String compressionType;
    /**
     * 重试发送次数
     * 默认1次
     */
    private Integer retries;
    /**
     * 事务ID前缀
     */
    private String transactionIdPrefix;
    /**
     * 是否开启事务
     */
    private Boolean transaction;

}
