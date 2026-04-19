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

import wang.bigbird.domain.framework.data.kafka.support.handler.KafkaConsumerHandler;

/**
 * kafka消费者服务
 * <p>
 * Kafka最初考虑的问题是，customer应该从brokes拉取消息还是brokers将消息推送到consumer，
 * 也就是pull还push。在这方面，Kafka遵循了一种大部分消息系统共同的传统的设计：
 * producer将消息推送到broker，consumer从broker拉取消息。
 * <p>
 * 一些消息系统比如Scribe和Apache Flume采用了push模式，将消息推送到下游的consumer。
 * 这样做有好处也有坏处：
 * 由broker决定消息推送的速率，对于不同消费速率的consumer就不太好处理了。
 * 消息系统都致力于让consumer以最大的速率最快速的消费消息，但不幸的是，
 * push模式下，当broker推送的速率远大于consumer消费的速率时，consumer恐怕就要崩溃了。
 * 最终Kafka还是选取了传统的pull模式。
 * Pull模式的另外一个好处是consumer可以自主决定是否批量的从broker拉取数据。
 * Push模式必须在不知道下游consumer消费能力和消费策略的情况下决定是立即推送每条消息还是缓存之后批量推送。
 * 如果为了避免consumer崩溃而采用较低的推送速率，将可能导致一次只推送较少的消息而造成浪费。
 * Pull模式下，consumer就可以根据自己的消费能力去决定这些策略。Pull有个缺点是，
 * 如果broker没有可供消费的消息，将导致consumer不断在循环中轮询，直到新消息到t达。
 * 为了避免这点，Kafka有个参数可以让consumer阻塞直到新消息到达
 * （当然也可以阻塞直到消息的数量达到某个特定的量就可以批量发）
 *
 * @author Bigbird
 */
public interface IKafkaConsumerService {

    /**
     * 消费消息
     *
     * @param topic        主题
     * @param groupId      消费组
     * @param messageClass 消息类型
     * @param handler      函数式处理
     */
    <T> void consume(String topic, String groupId,
                     Class<T> messageClass,
                     KafkaConsumerHandler<T> handler);
}
