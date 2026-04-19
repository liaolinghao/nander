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
package wang.bigbird.domain.framework.data.kafka.service.base.impl;

import org.apache.kafka.common.KafkaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import wang.bigbird.domain.framework.data.kafka.config.property.KafkaProducerProperties;
import wang.bigbird.domain.framework.data.kafka.support.handler.InProducerTransactionJobHandler;
import wang.bigbird.domain.framework.data.kafka.service.base.IKafkaProducerService;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * kafka生产者服务
 *
 * @author Bigbird
 */
@Service
public class KafkaProducerServiceImpl implements IKafkaProducerService {

    @Autowired
    private KafkaProducerProperties kafkaProducerProperties;

    @Autowired(required = false)
    @Qualifier("objectKafkaTemplate")
    private KafkaTemplate<String, Object> objectKafkaTemplate;

    @Autowired(required = false)
    @Qualifier("bytesKafkaTemplate")
    private KafkaTemplate<String, byte[]> bytesKafkaTemplate;

    @Override
    public void sendSync(String topic, Object message, InProducerTransactionJobHandler jobHandler) {
        ListenableFuture<SendResult<String, Object>> send = sendAsync(topic, message, jobHandler);
        waitDone(send, topic);
    }

    @Override
    public void sendSync(String topic, byte[] message, InProducerTransactionJobHandler jobHandler) {
        ListenableFuture<SendResult<String, byte[]>> send = sendAsync(topic, message, jobHandler);
        waitDone(send, topic);
    }

    @Override
    public ListenableFuture<SendResult<String, Object>> sendAsync(String topic, Object message, InProducerTransactionJobHandler jobHandler) {
        if (kafkaProducerProperties.getTransaction()) {
            return objectKafkaTemplate.executeInTransaction(operations -> {
                ListenableFuture<SendResult<String, Object>> sendResult = operations.send(topic, message);
                if (jobHandler != null) {
                    jobHandler.handle();
                }
                return sendResult;
            });
        } else {
            ListenableFuture<SendResult<String, Object>> sendResult = objectKafkaTemplate.send(topic, message);
            if (jobHandler != null) {
                jobHandler.handle();
            }
            return sendResult;
        }
    }

    @Override
    public ListenableFuture<SendResult<String, byte[]>> sendAsync(String topic, byte[] message, InProducerTransactionJobHandler jobHandler) {
        if (kafkaProducerProperties.getTransaction()) {
            return bytesKafkaTemplate.executeInTransaction(operations -> {
                ListenableFuture<SendResult<String, byte[]>> sendResult = operations.send(topic, message);
                if (jobHandler != null) {
                    jobHandler.handle();
                }
                return sendResult;
            });
        } else {
            ListenableFuture<SendResult<String, byte[]>> sendResult = bytesKafkaTemplate.send(topic, message);
            if (jobHandler != null) {
                jobHandler.handle();
            }
            return sendResult;
        }
    }

    private void waitDone(ListenableFuture<?> send, String topic) {
        try {
            send.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new KafkaException("Failed to send message, cause interruptedException, topic: " + topic, ie);
        } catch (ExecutionException | TimeoutException e) {
            throw new KafkaException("Failed to send message, topic: " + topic, e);
        }
    }

}
