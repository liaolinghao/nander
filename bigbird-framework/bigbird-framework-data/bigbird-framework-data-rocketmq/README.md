# Rocketmq构件

本构件用于管理Rocketmq相关的数据持久化操作。

```
// 生产数据服务
@Autowired
private RocketmqProducerService rocketmqProducerService;

// 消费数据服务
@Autowired
private RocketmqConsumerService rocketmqConsumerService;

```

## 配置

本构件自定义rocketmq配置。同时，本构件兼容spring原生的rocketmq配置。

配置加载优先级如下：

1、自定义配置优先

2、spring原生配置作为候补

具体描述如下：

```
# defaultMQProducer定义存在冲突，需要开启以下配置
spring:
  main:
    allow-bean-definition-overriding: true

bigbird:
  data:
    rocketmq:
      producer:
        key: # 加解密密钥
        address: # rocketmq集群地址中的某一个NameServer地址
        accessChannel: # 通道访问方式，可用值：LOCAL、CLOUD
        group: # 生产者组名
        accessKey: # 安全认证相关的key
        secretKey: # 安全认证相关的secret
        enableMsgTrace: # 是否打开消息轨迹，默认是false
        customizedTraceTopic: # 配置将消息轨迹数据存储到用户指定的Topic
        sendMessageTimeout: # 发送消息的超时时间，单位毫秒
        compressMessageBodyThreshold: # 消息超过设置的字节大小就开始压缩
        retryTimesWhenSendFailed: # 同步发送消息失败的重试次数
        retryTimesWhenSendAsyncFailed: # 异步发送消息失败的重试次数
        retryNextServer: # 开启内部消息重试
        maxMessageSize: # 限制消息的大小，单位字节，默认4M
      consumer:
        address: # rocketmq集群地址中的某一个NameServer地址
```
