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
package wang.bigbird.domain.framework.data.rocketmq.service.base;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import wang.bigbird.domain.framework.data.rocketmq.support.handler.RocketmqConsumerHandler;

/**
 * rocketmq消费者服务
 *
 * @author Bigbird
 */
public interface IRocketmqConsumerService {

    /**
     * 消费消息，消费者一旦创建，不能更改，因此每次调用该方法均会新建一个消费者
     *
     * @param consumerGroupName 消费者组名，需要保证组内的消费者订阅的topic都必须一致，组的意义在于集群模式下，当一个节点挂掉，
     *                          可以依靠组内其他节点继续工作。
     * @param topic             每个consumer必须且只能关注一个topic
     * @param tag               指定tag/key来进行过滤消息，支持通配符。*代表消费此topic下的全部消息，不进行过滤。
     * @param messageModel      消息消费模式，只有两种，默认集群模式，此时一条消息只会被一个消费者消费；广播模式则一条消息会被所有消费者消费。
     * @param messageClass      消息类型
     * @param handler           函数式处理
     */
    <T> void consume(String consumerGroupName, String topic, String tag,
                     MessageModel messageModel,
                     Class<T> messageClass,
                     RocketmqConsumerHandler<T> handler) throws MQClientException;

}
