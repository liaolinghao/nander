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

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.data.kafka.config.property.KafkaProducerProperties;
import wang.bigbird.domain.framework.data.kafka.service.base.IKafkaAdminService;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * kafka管理者服务
 *
 * @author Bigbird
 */
@Service
@Slf4j
public class KafkaAdminServiceImpl implements IKafkaAdminService {

    @Autowired
    private KafkaProducerProperties kafkaProducerProperties;

    @Autowired(required = false)
    private AdminClient adminClient;

    @Override
    public void createTopic(String name, int numPartitions, short replicationFactor) throws ExecutionException, InterruptedException {
        NewTopic newTopic = new NewTopic(name, numPartitions, replicationFactor);
        CreateTopicsResult result = adminClient.createTopics(Collections.singletonList(newTopic));
        result.all().get();
    }

    @Override
    public long countTopicMessages(String name) throws ExecutionException, InterruptedException {
        // 获取 Topic 描述（所有分区）
        TopicDescription topicDescription = adminClient.describeTopics(Collections.singleton(name)).values().get(name).get();
        List<TopicPartition> partitions = new ArrayList<>();
        topicDescription.partitions().forEach(p -> partitions.add(new TopicPartition(name, p.partition())));
        // 创建消费者（注意：KafkaConsumer非线程安全，必须每次新建）
        Properties consumerProps = new Properties();
        consumerProps.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaProducerProperties.getAddresses());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);
            Map<TopicPartition, Long> beginningOffsets = consumer.beginningOffsets(partitions);
            long total = 0;
            for (TopicPartition tp : partitions) {
                long end = endOffsets.get(tp);
                long begin = beginningOffsets.get(tp);
                total += (end - begin);
            }
            return total;
        }
    }

}
