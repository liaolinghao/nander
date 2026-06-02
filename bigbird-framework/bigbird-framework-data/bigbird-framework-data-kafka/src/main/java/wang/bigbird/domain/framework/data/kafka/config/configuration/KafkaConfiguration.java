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
package wang.bigbird.domain.framework.data.kafka.config.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.transaction.ChainedKafkaTransactionManager;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.data.kafka.base.helper.PropertiesHelper;
import wang.bigbird.domain.framework.data.kafka.base.tool.serializer.JsonSerializer;
import wang.bigbird.domain.framework.data.kafka.config.property.KafkaConsumerProperties;
import wang.bigbird.domain.framework.data.kafka.config.property.KafkaProducerProperties;
import wang.bigbird.domain.framework.data.kafka.support.condition.ProducerCondition;
import wang.bigbird.domain.framework.data.kafka.support.condition.TransactionCondition;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Kafka 配置
 *
 * @author Bigbird
 */
@Configuration
@Slf4j
@ComponentScan(basePackages = "wang.bigbird.domain.framework.data.kafka")
public class KafkaConfiguration {

    @PostConstruct
    public void init() {
        log.info("Init kafka framework.");
    }

    @Bean(destroyMethod = "close")
    @Conditional(ProducerCondition.class)
    public AdminClient adminClient(KafkaProducerProperties kafkaProducerProperties) {
        Properties properties = new Properties();
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaProducerProperties.getAddresses());
        return AdminClient.create(properties);
    }

    /**
     * 生产者工厂
     */
    @Bean
    @Primary
    @Conditional(ProducerCondition.class)
    public ProducerFactory kafkaProducerFactory(KafkaProducerProperties kafkaProducerProperties, KafkaConsumerProperties kafkaConsumerProperties, org.springframework.boot.autoconfigure.kafka.KafkaProperties springKafkaProperties) {
        PropertiesHelper.combineKafkaProperties(kafkaProducerProperties, kafkaConsumerProperties, springKafkaProperties);
        return buildProducerFactory(kafkaProducerProperties);
    }

    @Bean
    @Conditional(TransactionCondition.class)
    @ConditionalOnBean(DataSourceTransactionManager.class)
    public ChainedKafkaTransactionManager chainedKafkaTransactionManager(DataSourceTransactionManager transactionManager,
                                                                         KafkaTransactionManager<?, ?> kafkaTransactionManager) {
        return new ChainedKafkaTransactionManager<>(transactionManager, kafkaTransactionManager);
    }

    /**
     * KafkaTemplate（json序列化方式）
     */
    @Bean(name = "objectKafkaTemplate")
    @Conditional(ProducerCondition.class)
    public KafkaTemplate<String, Object> objectKafkaTemplate(ProducerFactory pf) {
        return new KafkaTemplate<>(pf);
    }

    /**
     * KafkaTemplate（原生类型）
     */
    @Bean(name = "bytesKafkaTemplate")
    @Conditional(ProducerCondition.class)
    public KafkaTemplate<String, byte[]> bytesKafkaTemplate(ProducerFactory pf) {
        return new KafkaTemplate<>(pf,
                Collections.singletonMap(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class));
    }

    /**
     * 生产者配置
     */
    private Map<String, Object> producerConfigs(KafkaProducerProperties kafkaProducerProperties) {
        Map<String, Object> props = new HashMap<>(CollectionUtils.initialMapCapacity(10));
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProducerProperties.getAddresses());
        Boolean transaction = kafkaProducerProperties.getTransaction();
        if (null != transaction && transaction) {
            // 事务模式下必须设置 retries=0
            props.put(ProducerConfig.RETRIES_CONFIG, 0);
        } else {
            props.put(ProducerConfig.RETRIES_CONFIG, kafkaProducerProperties.getRetries());
        }
        props.put(ProducerConfig.ACKS_CONFIG, kafkaProducerProperties.getAcks());
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, kafkaProducerProperties.getBatchSize().intValue());
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, kafkaProducerProperties.getBufferMemory());
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, kafkaProducerProperties.getCompressionType());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 60000);
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 8388608);
        return props;
    }

    private ProducerFactory buildProducerFactory(KafkaProducerProperties kafkaProducerProperties) {
        Map<String, Object> configs = producerConfigs(kafkaProducerProperties);
        DefaultKafkaProducerFactory factory = new DefaultKafkaProducerFactory(configs);
        Boolean transaction = kafkaProducerProperties.getTransaction();
        if (null != transaction && transaction) {
            factory.setTransactionIdPrefix(kafkaProducerProperties.getTransactionIdPrefix());
        }
        return factory;
    }

}
