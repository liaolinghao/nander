# Kafka构件

本构件用于管理Kafka相关的数据持久化操作。

对于事务的支持，虽然Spring提供了万能的@Transactional注解是可以用来管理kafka事务的，但是需要针对kafka做额外的配置管理。加之通常情况下，Spring的注解用于数据库事务处理，如果再结合数据库多数据源、分布式事务相关的处理，很有可能会造成不可预知的问题。加之，在本构件的开发过程中，发现采用注解支持事务，效果很不稳定（在topic无数据时事务生效，在topic已有数据时事务未生效），因此，经过综合考虑本构件仅采用kafka本地事务（executeInTransaction）的模式并提供了InProducerTransactionJobHandler接口，使用者只需要将事务相关的任务逻辑封装为一个InProducerTransactionJobHandler对象传递给类似sendSyncInTransaction模式的方法执行即可。

```
// 生产数据服务
@Autowired
private KafkaProducerService kafkaProducerService;

// 消费数据服务
@Autowired
private KafkaConsumerService kafkaConsumerService;

// 管理服务
@Autowired
private KafkaAdminService kafkaAdminService;
```

## 配置

本构件自定义kafka配置。同时，本构件兼容spring原生的kafka配置。

配置加载优先级如下：

1、自定义配置优先（spring原生配置transaction-id-prefix除外）

2、spring原生配置作为候补

具体描述如下：

```
# kafkaProducerFactory定义存在冲突，需要开启以下配置
spring:
  main:
    allow-bean-definition-overriding: true

bigbird:
  data:
    kafka:
      producer:
        addresses: # kafka集群地址，多个地址用,分开
        acks: # ack标识，默认"all"，可用值："0","1","-1","all"
        batchSize: # 批处理大小（以字节为单位），默认128KB
        bufferMemory: # 生产者可以用来缓冲等待发送到服务器的记录的内存总字节数，默认64M
        compressionType: # 生产者生成的所有数据的压缩类型，默认值为"none"，可以配置为"gzip"，"snappy"和"lz4"
        retries: # 重试发送次数，默认1，重试1次
        transactionIdPrefix: # 事务ID前缀，默认"tx-"
        transaction: # 是否开启事务，默认false，一旦开启，则生产者服务只能调用其中的事务性执行方法，否则会报错
      consumer:
        addresses: # kafka集群地址，多个地址用,分开
        enableAutoCommit: # 消费者的消费记录offset是否后台自动提交，默认true
        autoCommitInterval: # 当消费者的消费记录offset后台自动提交时，多长时间自动提交一次，单位毫秒，默认5000
        autoOffsetReset: # 当Kafka中没有初始偏移量或服务器上不再存在当前偏移量时的处理策略，可选值"earliest"，"latest"，"none"，"exception"，默认"latest"
        maxPollRecords: # 一次调用poll返回的最大记录数，默认100条
        fetchMaxWait: # 消费者拉取消息时的最长等待时间（单位：毫秒），默认5000毫秒
        fetchMinSize: # 返回消息给消费者的最小字节数阈值（以字节为单位），默认1字节
        heartbeatInterval: # 消费者协调员之间的心跳频率（单位是毫秒），默认3000毫秒
```

## 后续待完善事项

经过测试，在开启事务时，传入的任务处理器如果调用集成mybatis_plus的相关组件出错时，事务机制不会生效。
