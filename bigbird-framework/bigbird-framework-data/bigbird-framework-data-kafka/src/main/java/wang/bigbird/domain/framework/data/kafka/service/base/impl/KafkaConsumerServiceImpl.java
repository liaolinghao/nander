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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.tool.SystemClock;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.data.kafka.config.property.KafkaConsumerProperties;
import wang.bigbird.domain.framework.data.kafka.domain.pojo.DeadLetterRecord;
import wang.bigbird.domain.framework.data.kafka.exception.BatchNotFullyHandledException;
import wang.bigbird.domain.framework.data.kafka.service.base.IKafkaConsumerService;
import wang.bigbird.domain.framework.data.kafka.support.handler.KafkaConsumerHandler;
import wang.bigbird.domain.framework.data.kafka.support.handler.KafkaDeadLetterHandler;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * kafka消费者服务
 * <p>
 * 重要约束：
 * 1. 同一个 groupId 不要订阅多个不同topic，会发生跨topic rebalance
 * 2. 本实现：handler抛出异常 或 返回 false = 整批重试；反序列化异常单条丢弃，不污染同批次正常消息
 * 3. 语义：至少一次 at-least-once，业务必须幂等
 * 4. 失败消息交由死信处理器处理，无论结果如何不影响已有消费流程
 *
 * @author Bigbird
 */
@Service
@Slf4j
public class KafkaConsumerServiceImpl implements IKafkaConsumerService {

    private static final int POLL_TIMEOUT_MS = 1000;
    private static final int COMMIT_SYNC_TIMEOUT_SEC = 3;
    private static final int CONSUMER_CLOSE_TIMEOUT_SEC = 10;
    private static final long DESTROY_JOIN_TIMEOUT_MS = 10000L;

    @Autowired
    private KafkaConsumerProperties kafkaConsumerProperties;

    @Autowired(required = false)
    private KafkaDeadLetterHandler deadLetterHandler;

    private final Queue<ConsumerWorker> workers = new ConcurrentLinkedQueue<>();
    /**
     * groupId -> topic。
     * putIfAbsent 原子性同时防住两件事：
     * 1. 同 topic+groupId 重复启动；
     * 2. 同 groupId 订阅第二个 topic（重要约束1：跨 topic rebalance）。
     */
    private final ConcurrentHashMap<String, String> runningGroupTopicMap = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = JsonUtils.getMapper();

    /**
     * 消费线程与 consumer 的绑定，便于统一关闭
     */
    private static class ConsumerWorker {
        private final KafkaConsumer<String, byte[]> consumer;
        private final String groupId;
        private final String topic;
        private volatile boolean running = true;
        private volatile KafkaConsumerHandler.ConsumerContext consumerContext;
        private Thread thread;

        ConsumerWorker(KafkaConsumer<String, byte[]> consumer, String groupId, String topic) {
            this.consumer = consumer;
            this.groupId = groupId;
            this.topic = topic;
        }
    }

    /**
     * rebalance 监听
     * 只做日志观测，不再提交offset；牺牲一点重复消费，保证不丢消息（至少一次）
     */
    private static class RebalanceListener implements ConsumerRebalanceListener {

        private final String topic;
        private final String groupId;

        RebalanceListener(String topic, String groupId) {
            this.topic = topic;
            this.groupId = groupId;
        }

        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            log.info("[rebalance revoked] topic={}, groupId={}, partitions={}", topic, groupId, partitions);
        }

        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            log.info("[rebalance assigned] topic={}, groupId={}, partitions={}", topic, groupId, partitions);
        }

    }

    /**
     * 消费
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> void consume(String topic, String groupId,
                            Class<T> messageClass,
                            KafkaConsumerHandler<T> handler) {
        if (topic == null || topic.trim().isEmpty()) {
            throw new IllegalArgumentException("topic must not be empty");
        }
        if (groupId == null || groupId.trim().isEmpty()) {
            throw new IllegalArgumentException("groupId must not be empty");
        }
        if (messageClass == null) {
            throw new IllegalArgumentException("messageClass must not be null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler must not be null");
        }
        String prevTopic = runningGroupTopicMap.putIfAbsent(groupId, topic);
        if (prevTopic != null) {
            throw new IllegalArgumentException(prevTopic.equals(topic)
                    ? "consumer already running, topic=" + topic + ", groupId=" + groupId
                    : "groupId already subscribed topic=" + prevTopic
                    + ", cross-topic subscribe forbidden, groupId=" + groupId);
        }
        KafkaConsumer<String, byte[]> kafkaConsumer = null;
        ConsumerWorker worker = null;
        try {
            // 一个消费者只能属于一个消费者组，所以这里每次新建一个消费者
            kafkaConsumer = new KafkaConsumer<>(consumerConfigs(groupId, topic));
            worker = new ConsumerWorker(kafkaConsumer, groupId, topic);
            ConsumerWorker finalWorker = worker;
            Thread thread = new Thread(() -> run(messageClass, handler, finalWorker),
                    "kafka-consumer-" + topic + "-" + groupId);
            thread.setDaemon(false);
            thread.setUncaughtExceptionHandler((t, e) ->
                    log.error("kafka consumer thread uncaught exception. topic: {}", topic, e));
            worker.thread = thread;
            workers.add(worker);
            thread.start();
        } catch (Throwable e) {
            // 启动失败必须复位标记并关闭已创建资源，否则该 groupId 永久无法再启动
            runningGroupTopicMap.remove(groupId);
            if (kafkaConsumer != null) {
                try {
                    kafkaConsumer.close(Duration.ofSeconds(CONSUMER_CLOSE_TIMEOUT_SEC));
                } catch (Exception ce) {
                    log.warn("close kafka consumer after start failure error. topic: {}", topic, ce);
                }
            }
            if (worker != null) {
                workers.remove(worker);
            }
            throw e;
        }
        log.info("kafka consumer started. topic: {}, groupId: {}", topic, groupId);
    }

    /**
     * 注销 consumer：先通知线程退出，再等待线程内 finally 关闭 consumer。
     */
    @PreDestroy
    public void destroy() {
        log.info("stopping kafka consumers, size: {}", workers.size());
        for (ConsumerWorker worker : workers) {
            worker.running = false;
            KafkaConsumerHandler.ConsumerContext ctx = worker.consumerContext;
            if (ctx != null) {
                // 长任务 handler 立即感知 shutdown
                ctx.setRunning(false);
            }
            try {
                // wakeup 是唯一可以从外部线程安全调用的方法，用于中断 poll
                worker.consumer.wakeup();
            } catch (Exception e) {
                log.warn("wakeup kafka consumer error.", e);
            }
        }
        for (ConsumerWorker worker : workers) {
            try {
                worker.thread.join(DESTROY_JOIN_TIMEOUT_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        workers.clear();
        runningGroupTopicMap.clear();
        log.info("kafka consumers stopped.");
    }

    /**
     * 客户端配置
     */
    private Map<String, Object> consumerConfigs(String groupId, String topic) {
        Map<String, Object> props = Maps.newHashMapWithExpectedSize(14);
        // 同一个组内协同分配分区
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        // 只是一个名字标签，仅用于日志、监控、排查，不参与分区分配逻辑
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, topic + CommonConstants.DASHED + groupId + CommonConstants.DASHED + StringUtils.getUuid());
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConsumerProperties.getAddresses());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConsumerProperties.getAutoOffsetReset());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaConsumerProperties.getMaxPollRecords());
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, kafkaConsumerProperties.getMaxPollIntervalMs());
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, kafkaConsumerProperties.getFetchMaxWait());
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, kafkaConsumerProperties.getFetchMinSize());
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, kafkaConsumerProperties.getHeartbeatInterval());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, kafkaConsumerProperties.getMaxPartitionFetchBytes());
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaConsumerProperties.getSessionTimeoutMs());
        return props;
    }

    /**
     * 消费线程主体
     */
    private <T> void run(Class<T> messageClass,
                         KafkaConsumerHandler<T> handler,
                         ConsumerWorker worker) {
        KafkaConsumer<String, byte[]> consumer = worker.consumer;
        KafkaConsumerHandler.ConsumerContext consumerContext = new KafkaConsumerHandler.ConsumerContext();
        consumerContext.setRunning(true);
        worker.consumerContext = consumerContext;
        AtomicInteger failCount = new AtomicInteger(0);
        try {
            consumer.subscribe(Collections.singletonList(worker.topic), new RebalanceListener(worker.topic, worker.groupId));
            while (worker.running) {
                ConsumerRecords<String, byte[]> records = null;
                try {
                    records = consumer.poll(Duration.ofMillis(POLL_TIMEOUT_MS));
                    if (records.isEmpty()) {
                        continue;
                    }
                    List<byte[]> values = new ArrayList<>(records.count());
                    for (ConsumerRecord<String, byte[]> record : records) {
                        values.add(record.value());
                    }
                    // 传入上下文，业务长任务可感知shutdown
                    boolean allHandled = doHandle(messageClass, handler, values, consumerContext);
                    if (!allHandled) {
                        if (!consumerContext.isRunning()) {
                            // shutdown 信号已到达，业务主动放弃本批：不提交，未提交的 offset 下次启动重拉
                            log.info("consumer shutting down, abandon current batch without commit. topic={}, batchSize={}",
                                    worker.topic, records.count());
                            break;
                        }
                        // 运行中 handler 声明未完整处理：seek 回本批起点，按失败重试（限流 + 超限投死信）
                        exceptionHandle(worker, records, messageClass, failCount,
                                new BatchNotFullyHandledException("handler returned false, batch not fully handled"));
                        continue;
                    }
                    try {
                        // 消息处理成功，提交失败，不进入死信处理器，由于 offset 未提交，下次 poll 自然重拉重消费
                        consumer.commitSync(Duration.ofSeconds(COMMIT_SYNC_TIMEOUT_SEC));
                        log.debug("batch commit success, topic={}, batchSize={}", worker.topic, records.count());
                        failCount.set(0);
                    } catch (WakeupException we) {
                        // destroy() 触发的 wakeup：交给外层统一的 shutdown 处理
                        throw we;
                    } catch (CommitFailedException e) {
                        // rebalance 或处理超时导致提交失败，交给后续 rebalance 恢复。
                        // 注意：会重复消费，业务保持幂等处理。
                        log.error("commit failed, rebalance may happen. topic={}", worker.topic, e);
                    } catch (Exception e) {
                        log.error("commit sync error, will re-consume this batch. topic={}", worker.topic, e);
                    }
                } catch (WakeupException e) {
                    if (!worker.running) {
                        break;
                    }
                    throw e;
                } catch (Exception e) {
                    exceptionHandle(worker, records, messageClass, failCount, e);
                }
            }
        } catch (WakeupException e) {
            // shutting down
            log.info("kafka consumer waked up, exit. topic={}", worker.topic);
        } catch (Throwable e) {
            log.error("kafka consumer thread exit abnormally. topic: {}", worker.topic, e);
        } finally {
            consumerContext.setRunning(false);
            try {
                consumer.close(Duration.ofSeconds(CONSUMER_CLOSE_TIMEOUT_SEC));
            } catch (Exception e) {
                log.warn("close kafka consumer error. topic: {}", worker.topic, e);
            }
            workers.remove(worker);
            runningGroupTopicMap.remove(worker.groupId);
            log.info("kafka consumer stopped. topic: {}", worker.topic);
        }
    }

    /**
     * 计算各个分区本批次的最小 offset
     */
    private Map<TopicPartition, Long> calculatePartitionOffset(ConsumerRecords<String, byte[]> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<TopicPartition, Long> map = new HashMap<>(16);
        for (ConsumerRecord<String, byte[]> record : records) {
            TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
            long offset = record.offset();
            Long of = map.get(topicPartition);
            if (of == null || of > offset) {
                map.put(topicPartition, offset);
            }
        }
        return map;
    }

    /**
     * 计算各个分区本批次“下一条”offset，用于跳过毒消息批次
     */
    private Map<TopicPartition, OffsetAndMetadata> calculatePartitionNextOffset(
            ConsumerRecords<String, byte[]> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<TopicPartition, Long> maxOffsets = new HashMap<>(16);
        for (ConsumerRecord<String, byte[]> record : records) {
            TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
            maxOffsets.merge(topicPartition, record.offset(), Math::max);
        }
        Map<TopicPartition, OffsetAndMetadata> result = new HashMap<>(maxOffsets.size());
        maxOffsets.forEach((tp, offset) -> result.put(tp, new OffsetAndMetadata(offset + 1)));
        return result;
    }

    /**
     * 消息处理：反序列化失败的消息记录日志并跳过（视为已消费）。
     *
     * @return true 表示本批全部处理成功；false 表示业务未完整处理（如 shutdown 主动放弃），
     * 框架据此不提交当前 offset，避免未处理消息被跳过
     */
    @SuppressWarnings("unchecked")
    private <T> boolean doHandle(Class<T> messageClass,
                                 KafkaConsumerHandler<T> handler,
                                 List<byte[]> values, KafkaConsumerHandler.ConsumerContext consumerContext) {
        if (byte[].class.equals(messageClass)) {
            return handler.handle((List<T>) values, consumerContext);
        }
        List<T> valuesObjs = CollectionUtils.convertToList(values, value -> {
            if (value == null || value.length == 0) {
                log.warn("skip empty kafka raw message, messageClass={}", messageClass.getName());
                return null;
            }
            try {
                return objectMapper.readValue(value, messageClass);
            } catch (Exception e) {
                log.error("convert to object error, skip message. messageClass: {}",
                        messageClass.getName(), e);
                return null;
            }
        });
        if (!valuesObjs.isEmpty()) {
            return handler.handle(valuesObjs, consumerContext);
        }
        return true;
    }

    /**
     * 异常处理：限制重试次数，超过后跳过当前批次，避免毒消息永久阻塞。
     */
    private <T> void exceptionHandle(ConsumerWorker worker,
                                     ConsumerRecords<String, byte[]> records,
                                     Class<T> messageClass,
                                     AtomicInteger failCount,
                                     Exception e) {
        int count = failCount.incrementAndGet();
        if (e instanceof BatchNotFullyHandledException) {
            log.warn("batch not fully handled by handler. topic: {}, fail count: {}, message: {}",
                    worker.topic, count, e.getMessage());
        } else {
            log.error("consume msg exception. topic: {}, fail count: {}", worker.topic, count, e);
        }
        if (records == null || records.isEmpty()) {
            sleepBackoff(count, worker);
            return;
        }
        if (count > kafkaConsumerProperties.getMaxRetry()) {
            // 超过重试次数，跳过当前批次。
            skipBatchAfterRetryExceeded(worker, records, messageClass, e);
            failCount.set(0);
            return;
        }
        KafkaConsumer<String, byte[]> consumer = worker.consumer;
        // seek前校验assignment：只对当前自己持有的分区seek，防止rebalance后seek抛异常、静默丢消息
        Set<TopicPartition> assignment = consumer.assignment();
        Map<TopicPartition, Long> seekMap = calculatePartitionOffset(records);
        boolean hasValidSeek = false;
        for (Map.Entry<TopicPartition, Long> entry : seekMap.entrySet()) {
            TopicPartition tp = entry.getKey();
            if (assignment.contains(tp)) {
                try {
                    consumer.seek(tp, entry.getValue());
                    hasValidSeek = true;
                    log.warn("seek for retry, tp={}, offset={}", tp, entry.getValue());
                } catch (IllegalStateException ise) {
                    log.warn("seek failed, partition revoked during seek. tp={}", tp, ise);
                }
            } else {
                log.warn("skip seek, partition already revoked, tp={}", tp);
            }
        }
        if (!hasValidSeek) {
            log.warn("no available partition to seek, abandon retry for this batch, topic={}", worker.topic);
            return;
        }
        sleepBackoff(count, worker);
    }

    /**
     * 超过重试次数：先整批投死信，再 seek 到本批末尾 + 1 并同步提交，确保跳过且可追溯
     */
    private <T> void skipBatchAfterRetryExceeded(ConsumerWorker worker,
                                                 ConsumerRecords<String, byte[]> records,
                                                 Class<T> messageClass, Exception e) {
        log.error("retry exceed maxRetry={}, skip current batch. topic: {}",
                kafkaConsumerProperties.getMaxRetry(), worker.topic);
        // 1. 整批投死信（此时本批所有消息都要进死信，直接遍历 records 即可）
        sendToDeadLetter(worker, records, messageClass, e.getMessage());
        // 2. seek 到本批末尾 + 1 并同步提交
        KafkaConsumer<String, byte[]> consumer = worker.consumer;
        Map<TopicPartition, OffsetAndMetadata> nextOffsets = calculatePartitionNextOffset(records);
        Set<TopicPartition> assignment = consumer.assignment();
        Map<TopicPartition, OffsetAndMetadata> commitOffsets = Maps.newHashMapWithExpectedSize(nextOffsets.size());
        for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : nextOffsets.entrySet()) {
            TopicPartition tp = entry.getKey();
            if (!assignment.contains(tp)) {
                log.warn("skip seek for position msg, partition revoked, tp={}", tp);
                continue;
            }
            try {
                consumer.seek(tp, entry.getValue().offset());
                commitOffsets.put(tp, entry.getValue());
            } catch (Exception se) {
                log.warn("seek to skip failed. tp: {}", tp, se);
            }
        }
        if (commitOffsets.isEmpty()) {
            log.warn("no available partition to commit skip offsets, batch not skipped. topic={}", worker.topic);
            return;
        }
        try {
            // 显式提交跳过位置
            consumer.commitSync(commitOffsets, Duration.ofSeconds(COMMIT_SYNC_TIMEOUT_SEC));
            log.warn("commit skip offset success, batch skipped, topic={}", worker.topic);
        } catch (Exception ce) {
            log.error("commit skip offset failed, may repeat consume this batch, offsets: {}",
                    commitOffsets.keySet(), ce);
        }
    }

    private <T> boolean sendToDeadLetter(ConsumerWorker worker,
                                         ConsumerRecords<String, byte[]> records,
                                         Class<T> messageClass, String errorMessage) {
        if (deadLetterHandler == null) {
            log.warn("dead letter handler not configured, {} messages will be lost. topic: {}",
                    records.count(), worker.topic);
            return false;
        }
        List<DeadLetterRecord> dlqRecords = new ArrayList<>(records.count());
        for (ConsumerRecord<String, byte[]> record : records) {
            Headers copied = new RecordHeaders();
            record.headers().forEach(h -> copied.add(h));
            dlqRecords.add(DeadLetterRecord.builder()
                    .groupId(worker.groupId)
                    .originalTopic(record.topic())
                    .partition(record.partition())
                    .offset(record.offset())
                    .key(record.key())
                    .value(record.value())
                    .headers(copied)
                    .messageClass((Class<Object>) messageClass)
                    .errorMessage(errorMessage)
                    .sendTimestamp(SystemClock.now())
                    .build());
        }
        try {
            deadLetterHandler.sendBatch(dlqRecords);
            log.warn("sent {} messages to DLQ. topic: {}, errorMessage: {}",
                    dlqRecords.size(), worker.topic, errorMessage);
            return true;
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("send dead letters failed, skip anyway. topic: {}, count: {}",
                    worker.topic, dlqRecords.size(), e);
            return false;
        }
    }

    /**
     * 可被关闭打断的退避等待
     */
    private void sleepBackoff(int failCount, ConsumerWorker worker) {
        int ratio = Math.min((failCount / 3) + 1, 10);
        long sleepMs = ratio * 1000L;
        long slept = 0L;
        while (slept < sleepMs && worker.running) {
            try {
                long step = Math.min(200L, sleepMs - slept);
                Thread.sleep(step);
                slept += step;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

}
