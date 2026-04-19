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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.data.rocketmq.config.property.RocketmqConsumerProperties;
import wang.bigbird.domain.framework.data.rocketmq.service.base.IRocketmqConsumerService;
import wang.bigbird.domain.framework.data.rocketmq.support.handler.RocketmqConsumerHandler;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * rocketmq消费者服务
 *
 * @author Bigbird
 */
@Slf4j
@Service
public class RocketmqConsumerServiceImpl implements IRocketmqConsumerService {

    @Autowired
    private RocketmqConsumerProperties rocketmqConsumerProperties;

    /**
     * 记录消费者
     */
    private List<DefaultMQPushConsumer> consumers = new ArrayList<>();

    private final ObjectMapper objectMapper = JsonUtils.getMapper();

    @Override
    public <T> void consume(String consumerGroupName, String topic, String tag, MessageModel messageModel, Class<T> messageClass, RocketmqConsumerHandler<T> handler) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(
                consumerGroupName);
        consumer.setNamesrvAddr(rocketmqConsumerProperties.getAddress());
        consumer.subscribe(topic, tag);
        consumer.setMessageModel(messageModel == null ? MessageModel.CLUSTERING
                : messageModel);
        consumer.registerMessageListener((MessageListenerConcurrently) (records, consumeConcurrentlyContext) -> {
            List<byte[]> values = new ArrayList<>(records.size());
            for (MessageExt messageExt : records) {
                byte[] body = messageExt.getBody();
                values.add(body);
            }
            doHandle(messageClass, handler, values);
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumers.add(consumer);
        consumer.start();
    }

    @PreDestroy
    public void destroy() {
        if (CollectionUtils.isNotEmpty(consumers)) {
            for (DefaultMQPushConsumer consumer : consumers) {
                consumer.shutdown();
            }
        }
    }

    private <T> void doHandle(Class<T> messageClass, RocketmqConsumerHandler<T> handler, List<byte[]> values) {
        if (byte[].class.equals(messageClass)) {
            handler.handle((List<T>) values);
        } else {
            List<T> valuesObjs = values.stream()
                    .map(value -> {
                        try {
                            return objectMapper.readValue(value, messageClass);
                        } catch (IOException e) {
                            log.error("Covert to object error.", e);
                            return null;
                        }
                    }).collect(Collectors.toList());
            handler.handle(valuesObjs);
        }
    }


}
