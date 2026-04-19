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

import java.util.Map;

/**
 * rabbitmq生产者服务
 *
 * @author Bigbird
 */
public interface IRabbitmqProducerService {

    /**
     * 发送消息
     *
     * @param headers 自定义消息头
     * @param message 消息体
     * @param messageId 消息ID，能够唯一标识消息，消息不可达的时候触发ConfirmCallback回调方法时可以获取该值，进行对应的错误处理
     * @param exchangeName 交换机名
     * @param routingKey 路由KEY
     */
    <T> void sendMsg(Map<String, Object> headers, T message, String messageId, String exchangeName, String routingKey);

}
