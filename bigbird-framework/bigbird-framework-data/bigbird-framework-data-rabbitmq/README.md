# Rabbitmq构件

本构件用于管理Rabbitmq相关的数据持久化操作。

SpringBoot整合RabbitMQ，可以通过配置类创建交换机，创建队列，绑定交换机与队列：

```
// 定义队列
@Bean
public Queue queue() {
    return new Queue(QUEUE_NAME, true);
}

// 定义交换机  
@Bean
public XXXExchange exchange() {
    return new XXXExchange(EXCHANGE_NAME);
}

// 定义绑定   
@Bean
public Binding topicExchangeBingingOne() {
    return BindingBuilder.bind(queue()).to(exchange()).with(ROUTINGKEY);
}
```

但是这种模式适用于队列和交换器数量不多并且数量固定下的场景，当数量较多时，该模式会导致配置繁琐，此时，推荐采用RabbitmqAdminService进行操作。

```
// 生产数据服务
@Autowired
private RabbitmqProducerService rabbitmqProducerService;

// 消费数据服务
@Autowired
private RabbitmqConsumerService rabbitmqConsumerService;

// 管理服务
@Autowired
private RabbitmqAdminService rabbitmqAdminService;
```

## 配置

本构件自定义rabbitmq配置。同时，本构件兼容spring原生的rabbitmq配置。

配置加载优先级如下：

1、自定义配置优先

2、spring原生配置作为候补

具体描述如下：

```
# rabbitTemplate定义存在冲突，需要开启以下配置
spring:
  main:
    allow-bean-definition-overriding: true

bigbird:
  data:
    rabbitmq:
      key: # 加解密密钥
      addresses: # 节点地址，逗号分隔
      username: # 用户名
      password: # 密码
      virtualHost: # 虚拟主机
      receiveTimeout: # 主动拉取消息的超时时间
      replyTimeout: # 获取发送消息回复的超时时间
      concurrentConsumers: # 最小的消费者数量
      maxConcurrentConsumers: # 最大的消费者数量
```
