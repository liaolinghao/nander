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
package wang.bigbird.domain.framework.data.rocketmq.service.base.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.data.rocketmq.base.helper.MQProducerHelper;
import wang.bigbird.domain.framework.data.rocketmq.config.property.RocketmqProducerProperties;
import wang.bigbird.domain.framework.data.rocketmq.service.base.IRocketmqProducerService;

import javax.annotation.PreDestroy;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * rocketmq生产者服务
 *
 * @author Bigbird
 */
@Slf4j
@Service
public class RocketmqProducerServiceImpl implements IRocketmqProducerService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private RocketmqProducerProperties rocketmqProducerProperties;

    /**
     * 记录生产者
     */
    private Map<String, List<DefaultMQProducer>> producerMap = new HashMap<>();

    @Override
    public <T> void sendSync(String producerGroupName, String topic, String tag, T message, boolean newProducer) throws UnsupportedEncodingException, MQBrokerException, RemotingException, InterruptedException, MQClientException {
        Message msg = createMessage(topic, tag, message);
        DefaultMQProducer producer = getMQProducer(producerGroupName,
                newProducer);
        SendResult send = producer.send(msg);
        if (send.getSendStatus() != SendStatus.SEND_OK) {
            throw new RemotingException(StringUtils.joinStr("Send msg id:",
                    send.getMsgId(), ", Send status:", send.getSendStatus()));
        }
    }

    @Override
    public <T> void sendAsync(String producerGroupName, String topic, String tag, T message, boolean newProducer, SendCallback sendCallback) throws UnsupportedEncodingException, MQClientException, RemotingException, InterruptedException {
        Message msg = createMessage(topic, tag, message);
        DefaultMQProducer producer = getMQProducer(producerGroupName,
                newProducer);
        producer.send(msg, sendCallback);
    }

    @Override
    public <T> void sendMqOneway(String producerGroupName, String topic, String tag, T message, boolean newProducer) throws UnsupportedEncodingException, MQClientException, RemotingException, InterruptedException {
        Message msg = createMessage(topic, tag, message);
        DefaultMQProducer producer = getMQProducer(producerGroupName,
                newProducer);
        producer.sendOneway(msg);
    }

    @PreDestroy
    public void destroy() {
        if (MapUtils.isNotEmpty(producerMap)) {
            Set<Map.Entry<String, List<DefaultMQProducer>>> entrys = producerMap
                    .entrySet();
            for (Map.Entry<String, List<DefaultMQProducer>> entry : entrys) {
                List<DefaultMQProducer> producers = entry.getValue();
                for (DefaultMQProducer producer : producers) {
                    producer.shutdown();
                }
            }
        }
    }

    /**
     * 获取消息生产者
     *
     * @param producerGroupName 生产者组名，组的意义在于集群模式下，当一个节点挂掉，可以依靠组内其他节点继续工作。
     * @param newProducer       是否新建一个生产者
     * @return
     * @throws MQClientException
     */
    private DefaultMQProducer getMQProducer(String producerGroupName,
                                            boolean newProducer) throws MQClientException {
        if (StringUtils.isBlank(producerGroupName)) {
            return rocketMQTemplate.getProducer();
        }
        List<DefaultMQProducer> producers;
        if (producerMap.containsKey(producerGroupName)) {
            producers = producerMap.get(producerGroupName);
        } else {
            producers = new ArrayList<>();
            producerMap.put(producerGroupName, producers);
        }
        if (newProducer || producers.isEmpty()) {
            // 新建一个生产者
            DefaultMQProducer producer = MQProducerHelper.createDefaultMQProducer(producerGroupName, rocketmqProducerProperties);
            producer.start();
            producers.add(producer);
            return producer;
        } else {
            return producers.get(0);
        }
    }

    /**
     * 组装消息
     *
     * @param topic
     * @param tag
     * @param message
     * @param <T>
     * @return
     * @throws UnsupportedEncodingException
     */
    private <T> Message createMessage(String topic, String tag, T message) throws UnsupportedEncodingException {
        Message msg;
        if (message instanceof byte[]) {
            msg = new Message(topic, tag,
                    (byte[]) message);
        } else {
            msg = new Message(topic, tag,
                    JsonUtils.object2Json(message).getBytes(RemotingHelper.DEFAULT_CHARSET));
        }
        return msg;
    }
}
