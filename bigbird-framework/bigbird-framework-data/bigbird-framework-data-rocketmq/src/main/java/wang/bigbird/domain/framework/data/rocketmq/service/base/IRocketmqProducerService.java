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

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.io.UnsupportedEncodingException;

/**
 * rocketmq生产者服务
 *
 * @author Bigbird
 */
public interface IRocketmqProducerService {

    /**
     * 同步发送消息
     *
     * @param producerGroupName 生产者组名，组的意义在于集群模式下，当一个节点挂掉，可以依靠组内其他节点继续工作。
     * @param topic             主题，Message都有topic这一属性，Producer发送指定topic的消息，Consumer订阅Topic下的消息。通过Topic字段，Producer会获取消息投递的路由信息，决定发送给哪个Broker，topic如果不存在会自动创建。
     * @param tag               标签，不同的消费组，订阅同一topic不同的tag，拉取不同的消息并消费，在topic内部对消息进行隔离。
     * @param message           消息内容，Producer要发送的实际消息内容，以字节数组形式进行存储，message消息有一定大小限制。
     * @param newProducer       是否新建一个生产者。
     * @param <T>
     * @throws UnsupportedEncodingException
     * @throws MQBrokerException
     * @throws RemotingException
     * @throws InterruptedException
     * @throws MQClientException
     */
    <T> void sendSync(String producerGroupName, String topic, String tag, T message, boolean newProducer) throws UnsupportedEncodingException, MQBrokerException, RemotingException, InterruptedException, MQClientException;

    /**
     * 异步发送消息
     *
     * @param producerGroupName 生产者组名，组的意义在于集群模式下，当一个节点挂掉，可以依靠组内其他节点继续工作。
     * @param topic             主题，Message都有topic这一属性，Producer发送指定topic的消息，Consumer订阅Topic下的消息。通过Topic字段，Producer会获取消息投递的路由信息，决定发送给哪个Broker，topic如果不存在会自动创建。
     * @param tag               标签，不同的消费组，订阅同一topic不同的tag，拉取不同的消息并消费，在topic内部对消息进行隔离。
     * @param message           消息内容，Producer要发送的实际消息内容，以字节数组形式进行存储，message消息有一定大小限制。
     * @param newProducer       是否新建一个生产者。
     * @param sendCallback      接受消息发送结果的处理器。
     * @param <T>
     * @throws UnsupportedEncodingException
     * @throws RemotingException
     * @throws InterruptedException
     * @throws MQClientException
     */
    <T> void sendAsync(String producerGroupName, String topic, String tag, T message, boolean newProducer, SendCallback sendCallback) throws UnsupportedEncodingException, MQClientException, RemotingException, InterruptedException;

    /**
     * 投递消息，不考虑是否发送成功
     *
     * @param producerGroupName 生产者组名，组的意义在于集群模式下，当一个节点挂掉，可以依靠组内其他节点继续工作。
     * @param topic             主题，Message都有topic这一属性，Producer发送指定topic的消息，Consumer订阅Topic下的消息。通过Topic字段，Producer会获取消息投递的路由信息，决定发送给哪个Broker，topic如果不存在会自动创建。
     * @param tag               标签，不同的消费组，订阅同一topic不同的tag，拉取不同的消息并消费，在topic内部对消息进行隔离。
     * @param message           消息内容，Producer要发送的实际消息内容，以字节数组形式进行存储，message消息有一定大小限制。
     * @param newProducer       是否新建一个生产者。
     * @param <T>
     * @throws UnsupportedEncodingException
     * @throws RemotingException
     * @throws InterruptedException
     * @throws MQClientException
     */
    <T> void sendMqOneway(String producerGroupName, String topic,
                          String tag, T message, boolean newProducer) throws UnsupportedEncodingException, MQClientException, RemotingException, InterruptedException;
}
