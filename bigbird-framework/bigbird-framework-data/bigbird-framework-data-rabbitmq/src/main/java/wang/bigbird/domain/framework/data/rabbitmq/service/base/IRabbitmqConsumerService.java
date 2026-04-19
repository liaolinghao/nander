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
package wang.bigbird.domain.framework.data.rabbitmq.service.base;

import wang.bigbird.domain.framework.data.rabbitmq.support.handler.RabbitmqConsumerHandler;

/**
 * rabbitmq消费者服务
 * <p>
 * MQ有两种消息消费模式：
 * 轮询请求队列看是否有消息即拉模式，采用receive，receiveAndConvert和receiveAndReply方法实现；
 * 队列中有消息即对消费者进行通知即推模式，采用@RabbitListener注解实现；
 *
 * @author Bigbird
 */
public interface IRabbitmqConsumerService {

    /**
     * 消费指定队列的消息，采用rabbitTemplate.receiveAndConvert()方式循环主动拉取消息实现
     * 由于拉取消息会导致消息从队列中删除，因此采用该方法对于fanout和topic两种类型的队列无法保
     * 证每个消费端都能获得消息，如果要实现fanout和topic对应的消息被所有消费者处理，请采用@RabbitListener
     * 实现消息消费
     *
     * @param queueName    队列名
     * @param messageClass 消息类型
     * @param handler      函数式处理
     * @param <T>
     */
    <T> void consume(String queueName,
                     Class<T> messageClass,
                     RabbitmqConsumerHandler<T> handler);

    /**
     * 消费指定队列的消息，采用监听模式，效果等同于@RabbitListener
     *
     * @param queueName    队列名
     * @param messageClass 消息类型
     * @param handler      函数式处理
     * @param <T>
     */
    <T> void listen(String queueName,
                    Class<T> messageClass,
                    RabbitmqConsumerHandler<T> handler);
}
