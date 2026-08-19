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
package wang.bigbird.domain.framework.data.kafka.service.base;

import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import wang.bigbird.domain.framework.data.kafka.support.handler.InProducerTransactionJobHandler;

/**
 * kafka生产者服务
 *
 * @author Bigbird
 */
public interface IKafkaProducerService {

    /**
     * 执行同步发送消息
     *
     * @param topic      主题
     * @param message    消息
     * @param jobHandler 后继处理任务，一般用于开启事务模式下，消息发送到broke后的后续任务处理
     */
    void sendSync(String topic, Object message, InProducerTransactionJobHandler jobHandler);

    /**
     * 执行同步发送消息
     *
     * @param topic      主题
     * @param message    消息
     * @param jobHandler 后继处理任务，一般用于开启事务模式下，消息发送到broke后的后续任务处理
     */
    void sendSync(String topic, byte[] message, InProducerTransactionJobHandler jobHandler);

    /**
     * 执行同步发送消息（指定消息key，用于分区路由保证同key消息有序）
     *
     * @param topic      主题
     * @param key        消息key，相同key的消息会路由到同一分区
     * @param message    消息
     * @param jobHandler 后继处理任务，一般用于开启事务模式下，消息发送到broke后的后续任务处理
     */
    void sendSync(String topic, String key, Object message, InProducerTransactionJobHandler jobHandler);

    /**
     * 执行同步发送消息（指定消息key，用于分区路由保证同key消息有序）
     *
     * @param topic      主题
     * @param key        消息key，相同key的消息会路由到同一分区
     * @param message    消息
     * @param jobHandler 后继处理任务，一般用于开启事务模式下，消息发送到broke后的后续任务处理
     */
    void sendSync(String topic, String key, byte[] message, InProducerTransactionJobHandler jobHandler);

    /**
     * 执行异步发送消息
     *
     * @param topic      主题
     * @param message    消息
     * @param jobHandler 后继处理任务，一般用于开启事务模式下，消息发送到broke后的后续任务处理
     * @return ListenableFuture
     */
    ListenableFuture<SendResult<String, Object>> sendAsync(String topic, Object message, InProducerTransactionJobHandler jobHandler);

    /**
     * 执行异步发送消息
     *
     * @param topic      主题
     * @param message    消息
     * @param jobHandler 后继处理任务，一般用于开启事务模式下，消息发送到broke后的后续任务处理
     * @return ListenableFuture
     */
    ListenableFuture<SendResult<String, byte[]>> sendAsync(String topic, byte[] message, InProducerTransactionJobHandler jobHandler);

    /**
     * 执行异步发送消息（指定消息key，用于分区路由保证同key消息有序）
     *
     * @param topic      主题
     * @param key        消息key，相同key的消息会路由到同一分区
     * @param message    消息
     * @param jobHandler 后继处理任务，一般用于开启事务模式下，消息发送到broke后的后续任务处理
     * @return ListenableFuture
     */
    ListenableFuture<SendResult<String, Object>> sendAsync(String topic, String key, Object message, InProducerTransactionJobHandler jobHandler);

    /**
     * 执行异步发送消息（指定消息key，用于分区路由保证同key消息有序）
     *
     * @param topic      主题
     * @param key        消息key，相同key的消息会路由到同一分区
     * @param message    消息
     * @param jobHandler 后继处理任务，一般用于开启事务模式下，消息发送到broke后的后续任务处理
     * @return ListenableFuture
     */
    ListenableFuture<SendResult<String, byte[]>> sendAsync(String topic, String key, byte[] message, InProducerTransactionJobHandler jobHandler);

}
