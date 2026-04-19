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
package wang.bigbird.domain.framework.data.rabbitmq.service.base.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.data.rabbitmq.service.base.IRabbitmqProducerService;

import java.util.Map;

/**
 * rabbitmq生产者服务
 *
 * @author Bigbird
 */
@Slf4j
@Service
public class RabbitmqProducerServiceImpl implements IRabbitmqProducerService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public <T> void sendMsg(Map<String, Object> headers, T message, String messageId, String exchangeName, String routingKey) {
        MessageHeaders messageHeaders = new MessageHeaders(headers);
        Message<T> msg = MessageBuilder.createMessage(message, messageHeaders);
        CorrelationData correlationData = new CorrelationData(messageId);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, msg, correlationData);
    }
}
