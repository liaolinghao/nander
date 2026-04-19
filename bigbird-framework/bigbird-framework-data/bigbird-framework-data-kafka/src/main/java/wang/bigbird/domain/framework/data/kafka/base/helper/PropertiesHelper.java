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
package wang.bigbird.domain.framework.data.kafka.base.helper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.data.kafka.config.property.KafkaConsumerProperties;
import wang.bigbird.domain.framework.data.kafka.config.property.KafkaProducerProperties;

import java.util.List;

/**
 * 属性设置器
 *
 * @author Bigbird
 */
public class PropertiesHelper {

    /**
     * 合并kafka配置
     * <p>
     * 配置加载优先级如下：
     * <p>
     * 1、自定义配置优先
     * <p>
     * 2、spring原生配置作为候补
     *
     * @param kafkaProducerProperties 自定义kafka生产者属性
     * @param kafkaConsumerProperties 自定义kafka消费者属性
     * @param springKafkaProperties   spring原生的kafka配置
     */
    public static void combineKafkaProperties(KafkaProducerProperties kafkaProducerProperties, KafkaConsumerProperties kafkaConsumerProperties, KafkaProperties springKafkaProperties) {
        kafkaProducerProperties.setAddresses(loadAddresses(kafkaProducerProperties, springKafkaProperties));
        kafkaProducerProperties.setAcks(loadAcks(kafkaProducerProperties, springKafkaProperties));
        kafkaProducerProperties.setBatchSize(loadBatchSize(kafkaProducerProperties, springKafkaProperties));
        kafkaProducerProperties.setBufferMemory(loadBufferMemory(kafkaProducerProperties, springKafkaProperties));
        kafkaProducerProperties.setCompressionType(loadCompressionType(kafkaProducerProperties, springKafkaProperties));
        kafkaProducerProperties.setRetries(loadRetries(kafkaProducerProperties, springKafkaProperties));
        kafkaProducerProperties.setTransaction(loadTransaction(kafkaProducerProperties, springKafkaProperties));
        kafkaProducerProperties.setTransactionIdPrefix(loadTransactionIdPrefix(kafkaProducerProperties, springKafkaProperties));

        kafkaConsumerProperties.setAddresses(loadAddresses(kafkaConsumerProperties, springKafkaProperties));
        kafkaConsumerProperties.setEnableAutoCommit(loadEnableAutoCommit(kafkaConsumerProperties, springKafkaProperties));
        kafkaConsumerProperties.setAutoCommitInterval(loadAutoCommitInterval(kafkaConsumerProperties, springKafkaProperties));
        kafkaConsumerProperties.setAutoOffsetReset(loadAutoOffsetReset(kafkaConsumerProperties, springKafkaProperties));
        kafkaConsumerProperties.setMaxPollRecords(loadMaxPollRecords(kafkaConsumerProperties, springKafkaProperties));
        kafkaConsumerProperties.setFetchMaxWait(loadFetchMaxWait(kafkaConsumerProperties, springKafkaProperties));
        kafkaConsumerProperties.setFetchMinSize(loadFetchMinSize(kafkaConsumerProperties, springKafkaProperties));
        kafkaConsumerProperties.setHeartbeatInterval(loadHeartbeatInterval(kafkaConsumerProperties, springKafkaProperties));
    }

    private static Integer loadHeartbeatInterval(KafkaConsumerProperties kafkaConsumerProperties, KafkaProperties springKafkaProperties) {
        if (kafkaConsumerProperties.getHeartbeatInterval() == null) {
            if (springKafkaProperties.getConsumer().getHeartbeatInterval() != null) {
                return Long.valueOf(springKafkaProperties.getConsumer().getHeartbeatInterval().toMillis()).intValue();
            }
        } else {
            return kafkaConsumerProperties.getHeartbeatInterval();
        }
        return 3000;
    }

    private static Integer loadFetchMinSize(KafkaConsumerProperties kafkaConsumerProperties, KafkaProperties springKafkaProperties) {
        if (kafkaConsumerProperties.getFetchMinSize() == null) {
            if (springKafkaProperties.getConsumer().getFetchMinSize() != null) {
                return Long.valueOf(springKafkaProperties.getConsumer().getFetchMinSize().toBytes()).intValue();
            }
        } else {
            return kafkaConsumerProperties.getFetchMinSize();
        }
        return 1;
    }

    private static Integer loadFetchMaxWait(KafkaConsumerProperties kafkaConsumerProperties, KafkaProperties springKafkaProperties) {
        if (kafkaConsumerProperties.getFetchMaxWait() == null) {
            if (springKafkaProperties.getConsumer().getFetchMaxWait() != null) {
                return Long.valueOf(springKafkaProperties.getConsumer().getFetchMaxWait().toMillis()).intValue();
            }
        } else {
            return kafkaConsumerProperties.getFetchMaxWait();
        }
        return 5000;
    }

    private static Integer loadMaxPollRecords(KafkaConsumerProperties kafkaConsumerProperties, KafkaProperties springKafkaProperties) {
        if (kafkaConsumerProperties.getMaxPollRecords() == null) {
            if (springKafkaProperties.getConsumer().getMaxPollRecords() != null) {
                return springKafkaProperties.getConsumer().getMaxPollRecords();
            }
        } else {
            return kafkaConsumerProperties.getMaxPollRecords();
        }
        return 100;
    }

    private static String loadAutoOffsetReset(KafkaConsumerProperties kafkaConsumerProperties, KafkaProperties springKafkaProperties) {
        if (StringUtils.isBlank(kafkaConsumerProperties.getAutoOffsetReset())) {
            if (StringUtils.isNotBlank(springKafkaProperties.getConsumer().getAutoOffsetReset())) {
                return springKafkaProperties.getConsumer().getAutoOffsetReset();
            }
        } else {
            return kafkaConsumerProperties.getAutoOffsetReset();
        }
        return "latest";
    }

    private static Integer loadAutoCommitInterval(KafkaConsumerProperties kafkaConsumerProperties, KafkaProperties springKafkaProperties) {
        if (kafkaConsumerProperties.getAutoCommitInterval() == null) {
            if (springKafkaProperties.getConsumer().getAutoCommitInterval() != null) {
                return Long.valueOf(springKafkaProperties.getConsumer().getAutoCommitInterval().toMillis()).intValue();
            }
        } else {
            return kafkaConsumerProperties.getAutoCommitInterval();
        }
        return 5000;
    }

    private static Boolean loadEnableAutoCommit(KafkaConsumerProperties kafkaConsumerProperties, KafkaProperties springKafkaProperties) {
        if (kafkaConsumerProperties.getEnableAutoCommit() == null) {
            if (springKafkaProperties.getConsumer().getEnableAutoCommit() != null) {
                return springKafkaProperties.getConsumer().getEnableAutoCommit();
            }
        } else {
            return kafkaConsumerProperties.getEnableAutoCommit();
        }
        return true;
    }

    private static String loadAddresses(KafkaConsumerProperties kafkaConsumerProperties, KafkaProperties springKafkaProperties) {
        if (StringUtils.isBlank(kafkaConsumerProperties.getAddresses())) {
            List<String> bootstrapServers = null;
            if (CollectionUtils.isNotEmpty(springKafkaProperties.getConsumer().getBootstrapServers())) {
                bootstrapServers = springKafkaProperties.getConsumer().getBootstrapServers();
            } else if (CollectionUtils.isNotEmpty(springKafkaProperties.getBootstrapServers())) {
                bootstrapServers = springKafkaProperties.getBootstrapServers();
            }
            if (CollectionUtils.isNotEmpty(bootstrapServers)) {
                StringBuilder sb = new StringBuilder();
                for (String bootstrapServer : bootstrapServers) {
                    sb.append(",").append(bootstrapServer);
                }
                return sb.substring(1);
            }
        } else {
            return kafkaConsumerProperties.getAddresses();
        }
        return "127.0.0.1:9092";
    }

    private static Boolean loadTransaction(KafkaProducerProperties kafkaProducerProperties, KafkaProperties springKafkaProperties) {
        if (springKafkaProperties.getProducer().getTransactionIdPrefix() != null) {
            // spring原生的kafka配置可以开启事务，并设置事务前缀为“”串
            // 此配置较为特殊，以spring原生配置为第一优先级
            return true;
        }
        if (kafkaProducerProperties.getTransaction() == null) {
            return false;
        } else {
            return kafkaProducerProperties.getTransaction();
        }
    }

    private static String loadTransactionIdPrefix(KafkaProducerProperties kafkaProducerProperties, KafkaProperties springKafkaProperties) {
        if (StringUtils.isBlank(kafkaProducerProperties.getTransactionIdPrefix())) {
            if (StringUtils.isNotBlank(springKafkaProperties.getProducer().getTransactionIdPrefix())) {
                return springKafkaProperties.getProducer().getTransactionIdPrefix();
            }
        } else {
            return kafkaProducerProperties.getTransactionIdPrefix();
        }
        return "tx-";
    }

    private static Integer loadRetries(KafkaProducerProperties kafkaProducerProperties, KafkaProperties springKafkaProperties) {
        if (kafkaProducerProperties.getRetries() == null) {
            if (springKafkaProperties.getProducer().getRetries() != null) {
                return springKafkaProperties.getProducer().getRetries();
            }
        } else {
            return kafkaProducerProperties.getRetries();
        }
        return 1;
    }

    private static String loadCompressionType(KafkaProducerProperties kafkaProducerProperties, KafkaProperties springKafkaProperties) {
        if (StringUtils.isBlank(kafkaProducerProperties.getCompressionType())) {
            if (StringUtils.isNotBlank(springKafkaProperties.getProducer().getCompressionType())) {
                return springKafkaProperties.getProducer().getCompressionType();
            }
        } else {
            return kafkaProducerProperties.getCompressionType();
        }
        return "none";
    }

    private static Long loadBufferMemory(KafkaProducerProperties kafkaProducerProperties, KafkaProperties springKafkaProperties) {
        if (kafkaProducerProperties.getBufferMemory() == null) {
            if (springKafkaProperties.getProducer().getBufferMemory() != null) {
                return springKafkaProperties.getProducer().getBufferMemory().toBytes();
            }
        } else {
            return kafkaProducerProperties.getBufferMemory();
        }
        return 67108864L;
    }

    private static Long loadBatchSize(KafkaProducerProperties kafkaProducerProperties, KafkaProperties springKafkaProperties) {
        if (kafkaProducerProperties.getBatchSize() == null) {
            if (springKafkaProperties.getProducer().getBatchSize() != null) {
                return springKafkaProperties.getProducer().getBatchSize().toBytes();
            }
        } else {
            return kafkaProducerProperties.getBatchSize();
        }
        return 131072L;
    }

    private static String loadAcks(KafkaProducerProperties kafkaProducerProperties, KafkaProperties springKafkaProperties) {
        if (StringUtils.isBlank(kafkaProducerProperties.getAcks())) {
            if (StringUtils.isNotBlank(springKafkaProperties.getProducer().getAcks())) {
                return springKafkaProperties.getProducer().getAcks();
            }
        } else {
            return kafkaProducerProperties.getAcks();
        }
        return "all";
    }

    private static String loadAddresses(KafkaProducerProperties kafkaProducerProperties, KafkaProperties springKafkaProperties) {
        if (StringUtils.isBlank(kafkaProducerProperties.getAddresses())) {
            List<String> bootstrapServers = null;
            if (CollectionUtils.isNotEmpty(springKafkaProperties.getProducer().getBootstrapServers())) {
                bootstrapServers = springKafkaProperties.getProducer().getBootstrapServers();
            } else if (CollectionUtils.isNotEmpty(springKafkaProperties.getBootstrapServers())) {
                bootstrapServers = springKafkaProperties.getBootstrapServers();
            }
            if (CollectionUtils.isNotEmpty(bootstrapServers)) {
                StringBuilder sb = new StringBuilder();
                for (String bootstrapServer : bootstrapServers) {
                    sb.append(",").append(bootstrapServer);
                }
                return sb.substring(1);
            }
        } else {
            return kafkaProducerProperties.getAddresses();
        }
        return "127.0.0.1:9092";
    }


}
