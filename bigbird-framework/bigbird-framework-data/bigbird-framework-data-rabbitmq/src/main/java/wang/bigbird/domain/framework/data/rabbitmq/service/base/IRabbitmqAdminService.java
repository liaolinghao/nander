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

import com.rabbitmq.client.BuiltinExchangeType;

/**
 * rabbitmq管理者服务
 *
 * @author Bigbird
 */
public interface IRabbitmqAdminService {

    /**
     * 定义交换机，定义队列，将队列与交换机进行绑定
     *
     * @param exchangeName 交换机名
     * @param queueName    队列名
     * @param routingKey   路由KEY
     * @param type         消息模式：FANOUT|TOPIC|DIRECT
     * @param durable      是否持久化
     * @param autoDelete   是否自动删除交换机
     *                     <p>
     *                     自动删除的条件是向后的
     *                     <p>
     *                     对于exchange交换器，向前是生产端发布的消息和routingKey，
     *                     这不能作为exchange自动删除的条件。exchange向后是绑定另一个交换器，
     *                     或者绑定队列。这就是exchange交换器删除的条件。
     *                     总结：exchange自动删除的条件，有队列或者交换器绑定了本交换器，
     *                     然后所有队列或交换器都与本交换器解除绑定，autoDelete=true时，此交换器就会被自动删除。
     *                     <p>
     *                     对于队列，向前是与exchange的绑定关系，这不能作为队列自动删除的条件。 队列向后是被消费者订阅。这就是队列删除的条件。
     *                     总结：队列自动删除的条件，有消息者订阅本队列，然后所有消费者都解除订阅此队列，
     *                     autoDelete=true时，此队列会自动删除，即使此队列中还有消息。
     */
    void queueBind(String exchangeName, String queueName,
                   String routingKey, BuiltinExchangeType type, boolean durable,
                   boolean autoDelete);

}
