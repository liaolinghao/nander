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
package wang.bigbird.domain.framework.data.rabbitmq.config.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import wang.bigbird.domain.framework.data.rabbitmq.base.helper.PropertiesHelper;
import wang.bigbird.domain.framework.data.rabbitmq.config.property.RabbitmqProperties;
import wang.bigbird.domain.framework.data.rabbitmq.support.handler.MessageHandler;

import javax.annotation.PostConstruct;

/**
 * Rabbitmq 配置
 *
 * @author Bigbird
 */
@Configuration
@Slf4j
@ComponentScan(basePackages = "wang.bigbird.domain.framework.data.rabbitmq")
public class RabbitmqConfiguration {

    @PostConstruct
    public void init() {
        log.info("Init rabbitmq framework.");
    }

    @Bean
    public ConnectionFactory connectionFactory(RabbitmqProperties rabbitmqProperties, org.springframework.boot.autoconfigure.amqp.RabbitProperties springRabbitmqProperties) {
        PropertiesHelper.combineRabbitmqProperties(rabbitmqProperties, springRabbitmqProperties);
        return buildConnectionFactory(rabbitmqProperties);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        // 只有设置为true，spring才会加载RabbitAdmin这个类
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, RabbitmqProperties rabbitmqProperties) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setReplyTimeout(rabbitmqProperties.getReplyTimeout());
        rabbitTemplate.setReceiveTimeout(rabbitmqProperties.getReceiveTimeout());
        //设置开启Mandatory，才能触发回调函数，无论消息推送结果怎么样都强制调用回调函数
        rabbitTemplate.setMandatory(true);
        /* 确认的回调，确认消息是否到达交换器
         * 如果发送时候指定的交换器不存在，ack就是false，代表消息不可达
         */
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            log.info("CorrelationData: {}, Ack: {}.", correlationData.getId(), ack);
            if (!ack) {
                log.error("Failed to send messages {} to the exchange, cause:{}.", correlationData.getId(), cause);
            }
        });
        /* 消息失败的回调，确认消息是否到达队列
         * 如果路由键匹配的绑定到该交换器的队列不存在，会触发这个回调，此时replyText:NO_ROUTE
         */
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchangeName, routingKey) -> {
            log.error("Message:{}; ReplyCode:{}; ReplyText:{}; ExchangeName:{}; RoutingKey:{}.",
                    message, replyCode, replyText, exchangeName, routingKey);
        });
        return rabbitTemplate;
    }

    @Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer(ConnectionFactory connectionFactory, RabbitmqProperties rabbitmqProperties) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setConcurrentConsumers(rabbitmqProperties.getConcurrentConsumers());
        container.setMaxConcurrentConsumers(rabbitmqProperties.getMaxConcurrentConsumers());
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            String queueName = message.getMessageProperties().getConsumerQueue();
            try {
                MessageHandler.parseMessage(message, MessageHandler.queueMessageClassMap.get(queueName), MessageHandler.queueMessageConsumerHandlerMap.get(queueName));
                // 第二个参数，手动确认可以被批处理
                // 当设置true，可以一次性确认delivery_tag小于等于传入值的所有消息
                channel.basicAck(deliveryTag, true);
            } catch (Exception e) {
                // 第二个参数，true会重新放回队列尾部，导致队列消息顺序变更
                // 谨慎设置true，以防止陷入反复错误循环处理，应该在日志记录消息体，手工分析处理
                channel.basicReject(deliveryTag, false);
                log.error("Consume msg exception, message: {}.", message);
                log.error("Consume msg exception, queueName: {}.", queueName, e);
            }
        });
        return container;
    }

    private ConnectionFactory buildConnectionFactory(RabbitmqProperties rabbitmqProperties) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses(rabbitmqProperties.getAddresses());
        connectionFactory.setUsername(rabbitmqProperties.getUsername());
        connectionFactory.setPassword(rabbitmqProperties.getPassword());
        connectionFactory.setVirtualHost(rabbitmqProperties.getVirtualHost());
        // 设置消息回调
        // 确认消息已发送到交换机(Exchange)
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        // 确认消息已发送到队列(Queue)
        connectionFactory.setPublisherReturns(true);
        return connectionFactory;
    }
}
