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

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.data.kafka.config.property.KafkaConsumerProperties;
import wang.bigbird.domain.framework.data.kafka.support.handler.KafkaConsumerHandler;
import wang.bigbird.domain.framework.data.kafka.service.base.IKafkaConsumerService;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * kafka消费者服务
 *
 * @author Bigbird
 */
@Service
@Slf4j
public class KafkaConsumerServiceImpl implements IKafkaConsumerService {

    @Autowired
    private KafkaConsumerProperties kafkaConsumerProperties;

    private final List<KafkaConsumer<String, byte[]>> kafkaConsumers = new CopyOnWriteArrayList<>();

    private final ObjectMapper objectMapper = JsonUtils.getMapper();

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNamePrefix("kafka-consumer-thread-").build();
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
    public <T> void consume(String topic, String groupId,
                            Class<T> messageClass,
                            KafkaConsumerHandler<T> handler) {
        consumerExecutor.execute(() -> {
            // 一个消费者只能属于一个消费者组，所以这里每次新建一个消费者
            Map<String, Object> configs = consumerConfigs(groupId);
            KafkaConsumer<String, byte[]> kafkaConsumer = new KafkaConsumer<>(configs);
            kafkaConsumers.add(kafkaConsumer);
            kafkaConsumer.subscribe(Collections.singletonList(topic));
            AtomicInteger failCount = new AtomicInteger(0);
            while (true) {
                ConsumerRecords<String, byte[]> records = null;
                try {
                    records = kafkaConsumer.poll(Duration.ofMillis(1000));
                    if (null == records || records.isEmpty()) {
                        continue;
                    }
                    List<byte[]> values = new ArrayList<>(records.count());
                    for (ConsumerRecord<String, byte[]> record : records) {
                        byte[] value = record.value();
                        values.add(value);
                    }
                    doHandle(messageClass, handler, values);
                    kafkaConsumer.commitSync();
                    failCount.set(0);
                } catch (CommitFailedException e) {
                    log.error("Commit failed. topic: {}.", topic, e);
                } catch (Exception e) {
                    exceptionHandle(kafkaConsumer, records, topic, failCount, e);
                }
            }
        });
    }

    /**
     * 注销
     */
    @PreDestroy
    public void destroy() {
        kafkaConsumers.forEach(KafkaConsumer::close);
        consumerExecutor.shutdown();
    }


    /**
     * 客户端配置
     *
     * @param groupId
     * @return
     */
    private Map<String, Object> consumerConfigs(String groupId) {
        Map<String, Object> props = new HashMap<>(CollectionUtils.initialMapCapacity(13));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConsumerProperties.getAddresses());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaConsumerProperties.getEnableAutoCommit());
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, kafkaConsumerProperties.getAutoCommitInterval());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConsumerProperties.getAutoOffsetReset());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaConsumerProperties.getMaxPollRecords());
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, kafkaConsumerProperties.getFetchMaxWait());
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, kafkaConsumerProperties.getFetchMinSize());
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, kafkaConsumerProperties.getHeartbeatInterval());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "8388608");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "60000");
        return props;
    }

    /**
     * 计算各个分区offset
     *
     * @param records
     * @return
     */
    private Map<TopicPartition, Long> calculatePartitionOffset(ConsumerRecords<String, byte[]> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<TopicPartition, Long> map = new HashMap<>(CollectionUtils.initialMapCapacity(records.count()));
        for (ConsumerRecord<String, byte[]> record : records) {
            String topic = record.topic();
            long offset = record.offset();
            TopicPartition topicPartition = new TopicPartition(topic, record.partition());
            Long of = map.get(topicPartition);
            if (of == null) {
                map.put(topicPartition, offset);
            } else {
                if (of > offset) {
                    map.put(topicPartition, offset);
                }
            }
        }
        return map;
    }

    /**
     * 消息处理
     *
     * @param messageClass
     * @param handler
     * @param values
     * @param <T>
     */
    private <T> void doHandle(Class<T> messageClass, KafkaConsumerHandler<T> handler, List<byte[]> values) {
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


    /**
     * 异常处理
     *
     * @param kafkaConsumer
     * @param records
     * @param topic
     * @param failCount
     * @param e
     */
    private void exceptionHandle(KafkaConsumer<String, byte[]> kafkaConsumer,
                                 ConsumerRecords<String, byte[]> records,
                                 String topic,
                                 AtomicInteger failCount,
                                 Exception e) {
        failCount.addAndGet(1);
        log.error("Consume msg exception. topic: {}, fail count: {}.", topic, failCount.get(), e);
        if (records != null && !records.isEmpty()) {
            Map<TopicPartition, Long> map = calculatePartitionOffset(records);
            map.forEach(kafkaConsumer::seek);
        }
        try {
            int ratio = (failCount.get() / 3) + 1;
            ratio = Math.min(ratio, 10);
            Thread.sleep(ratio * 1000L);
        } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
        }
    }

}
