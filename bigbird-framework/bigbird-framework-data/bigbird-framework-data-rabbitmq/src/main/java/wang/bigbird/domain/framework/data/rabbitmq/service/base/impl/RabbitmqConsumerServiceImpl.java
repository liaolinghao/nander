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

import cn.hutool.core.thread.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.data.rabbitmq.support.handler.MessageHandler;
import wang.bigbird.domain.framework.data.rabbitmq.support.handler.RabbitmqConsumerHandler;
import wang.bigbird.domain.framework.data.rabbitmq.service.base.IRabbitmqConsumerService;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;

/**
 * rabbitmq消费者服务
 *
 * @author Bigbird
 */
@Slf4j
@Service
public class RabbitmqConsumerServiceImpl implements IRabbitmqConsumerService {

    @Autowired
    private SimpleMessageListenerContainer simpleMessageListenerContainer;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNamePrefix("rabbitmq-consumer-thread-").build();
    private final ExecutorService consumerExecutor = new ThreadPoolExecutor(
            // 核心线程数
            5,
            // 最大线程数
            10,
            // 空闲线程存活时间
            60L, TimeUnit.SECONDS,
            // 工作队列
            new LinkedBlockingQueue<>(100), threadFactory,
            // 拒绝策略
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @Override
    public <T> void consume(String queueName, Class<T> messageClass, RabbitmqConsumerHandler<T> handler) {
        consumerExecutor.execute(() -> {
            while (true) {
                Message message = null;
                try {
                    message = rabbitTemplate.receive(queueName);
                    if (message == null) {
                        continue;
                    }
                    MessageHandler.parseMessage(message, messageClass, handler);
                } catch (Exception e) {
                    if (message != null) {
                        log.error("Consume msg exception, message: {}.", message);
                    }
                    log.error("Consume msg exception, queueName: {}.", queueName, e);
                }
            }
        });
    }

    @Override
    public <T> void listen(String queueName, Class<T> messageClass, RabbitmqConsumerHandler<T> handler) {
        MessageHandler.queueMessageClassMap.put(queueName, messageClass);
        MessageHandler.queueMessageConsumerHandlerMap.put(queueName, handler);
        simpleMessageListenerContainer.addQueueNames(queueName);
    }

    /**
     * 注销
     */
    @PreDestroy
    public void destroy() {
        consumerExecutor.shutdown();
    }

}
