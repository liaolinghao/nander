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

import com.rabbitmq.client.BuiltinExchangeType;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.data.rabbitmq.service.base.IRabbitmqAdminService;

/**
 * rabbitmq管理者服务
 *
 * @author Bigbird
 */
@Service
public class RabbitmqAdminServiceImpl implements IRabbitmqAdminService {

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Override
    public void queueBind(String exchangeName, String queueName, String routingKey, BuiltinExchangeType type, boolean durable, boolean autoDelete) {
        // 声明交换机类型：交换机，类型，持久化
        rabbitAdmin.declareExchange(exchangeDeclare(exchangeName, type, durable, autoDelete));
        // 声明队列：队列，持久化，声明独占队列（仅限于此连接），自动删除队列，队列的其他属性
        rabbitAdmin.declareQueue(new Queue(queueName, durable, false, false, null));
        // 将队列与交换机绑定
        rabbitAdmin.declareBinding(new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, routingKey, null));
    }

    /**
     * 创建指定类型的交换机
     *
     * @param exchangeName
     * @param type
     * @param durable
     * @param autoDelete
     * @return
     */
    private Exchange exchangeDeclare(String exchangeName, BuiltinExchangeType type, boolean durable, boolean autoDelete) {
        switch (type) {
            case DIRECT:
                return new DirectExchange(exchangeName, durable, autoDelete);
            case FANOUT:
                return new FanoutExchange(exchangeName, durable, autoDelete);
            case HEADERS:
                return new HeadersExchange(exchangeName, durable, autoDelete);
            case TOPIC:
                return new TopicExchange(exchangeName, durable, autoDelete);
            default:
                throw new IllegalArgumentException("The exchange type is invalid.");
        }
    }
}
