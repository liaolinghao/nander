# SERVER统一RPC能力构件核心模块

本构件属于SERVER统一RPC能力的核心模块，提供以下功能：

1、基于Dubbo对外提供RPC接口服务，可提供Dubbo服务地址信息获取功能。

2、集成Nacos为注册和配置中心，提供往Nacos注册服务元数据功能。

3、提供了支撑RPC高性能序列化方式kryo的类注册机制，通过扫描指定包下被@KryoSerializable注解的类实现。

## 配置

本构件有以下重要配置，描述如下：

```
spring:
  application:
    name: xxx-xxx

# Dubbo 基础通用配置（提供者 / 消费者共用）
dubbo:
  # 应用基础配置（核心）
  application:
    # Dubbo 应用名（需与 spring.application.name 一致）
    name: xxx-xxx
    # 负责人（可选）
    owner: bigbird
    # 组织名（可选）
    organization: xxx
    # 日志框架适配（对齐 Spring Boot 日志）
    logger: slf4j
    # QOS安全配置
    # 禁用QOS服务，防止未授权访问
    qos-enable: false
    # 禁止外部IP访问QOS
    qos-accept-foreign-ip: false
  # 注册中心配置（Nacos）
  registry:
    # Nacos 注册中心地址（集群用逗号分隔：127.0.0.1:8848,127.0.0.1:8849）
    address: nacos://${NACOS_ADDRESS:127.0.0.1:8848}
    # 注册中心协议（固定为 nacos）
    protocol: nacos
    # 注册中心连接超时（ms）
    timeout: ${NACOS_TIMEOUT:5000}
    # Nacos 命名空间（可选，隔离环境：dev/test/prod）
    parameters:
      # Nacos 命名空间 ID（环境隔离，必填）
      namespace: ${NACOS_DUBBO_NAMESPACE:}
      # 服务分组（同一个系统内的服务分组必须一致，否则不能发现服务提供者）
      group: ${NACOS_DUBBO_GROUP:xx-platform}
      # Nacos 用户名（若开启认证）
      username: ${NACOS_USERNAME:nacos}
      # Nacos 密码（若开启认证）
      password: ${NACOS_PWD:nacos}
      # 兼容 Nacos 2.x 鉴权的备用参数
      accessKey: ${NACOS_USERNAME:nacos}
      # 兼容 Nacos 2.x 鉴权的备用参数
      secretKey: ${NACOS_PWD:nacos}
  # 元数据配置（2.7.x 新增，优化服务发现）
  metadata-report:
    # 元数据存储到 Nacos
    address: nacos://${NACOS_ADDRESS:127.0.0.1:8848}
    # 发布失败重试次数
    retry-times: 3
    # 超时时间（毫秒）
    timeout: 10000
    parameters:
      namespace: ${NACOS_DUBBO_NAMESPACE:}
      # Nacos 用户名（若开启认证）
      username: ${NACOS_USERNAME:nacos}
      # Nacos 密码（若开启认证）
      password: ${NACOS_PWD:nacos}
      # 兼容 Nacos 2.x 鉴权的备用参数
      accessKey: ${NACOS_USERNAME:nacos}
      # 兼容 Nacos 2.x 鉴权的备用参数
      secretKey: ${NACOS_PWD:nacos}
  # 监控配置（可选，对接 Dubbo Admin）
  monitor:
    # 指定协议为none，禁用监控
    protocol: ${DUBBO_MONITOR_PROTOCOL:none}
  # 协议配置（服务暴露协议）
  protocol:
    # 通信协议（dubbo/triple/grpc 等）
    name: dubbo
    # 服务端口（-1为随机端口）
    port: ${DUBBO_PORT:-1}
    # 核心：注册到Nacos的IP（读取Compose传递的宿主机IP，否则会自动采用容器内网IP）
    host: ${DUBBO_HOST:127.0.0.1}
    # 单个端口最大接受连接数
    accepts: ${DUBBO_ACCEPTS:1000}
    # 最大请求数据量（8M，默认）
    payload: ${DUBBO_PAYLOAD:8388608}
    # 序列化方式，可取值如下：
    # hessian2 默认序列化，跨语言二进制，兼容性最好，内网通用
    # dub 阿里自研二进制序列化，性能优于 hessian2，但生态兼容性差，生产不推荐
    # kryo 二进制体积远小于 hessian2，序列化 / 反序列化 CPU 开销更低，纯 Java 内网场景首选
    serialization: ${DUBBO_SERIALIZATION:kryo}
  # 提供者全局配置
  provider:
    # 线程池策略，采用固定线程池能避免线程频繁创建导致的额外开销
    threadpool: ${DUBBO_PROVIDER_THREADPOOL:fixed}
    # 线程数按 CPU核数 * 100 ~ 120 区间选取
    threads: ${DUBBO_PROVIDER_THREADS:100}
    # 缓冲队列，应对瞬时脉冲流量，threads × 1.0 ~ 1.2
    queues: ${DUBBO_PROVIDER_QUEUES:120}
    # 默认超时时间（ms），限制处理单个请求的最长时间（从接收请求到返回响应）
    # 超时未处理完毕则主动中断处理，返回超时异常
    timeout: ${DUBBO_PROVIDER_TIMEOUT:10000}
    # 限制消费端允许的重试次数（非幂等写操作必须 0，读操作可 1-2）
    retries: ${DUBBO_PROVIDER_RETRIES:0}
    # 延迟暴露（-1 为 Spring 容器启动完成后暴露）
    delay: -1
    # 开启参数校验（需引入 dubbo-validation 依赖）
    validation: false
    # 集群容错策略（failfast：快速失败，可选 failover/failsafe）
    cluster: failfast
    # 开启服务令牌（防止非法调用）
    token: ${DUBBO_TOKEN_ENABLE:true}
    parameters:
      # 核心配置：指定需要透传的自定义异常全类名，多个用逗号分隔
      exceptions: wang.bigbird.domain.framework.server.core.exception.BusinessException
      # Dubbo安全令牌（防止未授权访问）- 使用固定token
      token: ${DUBBO_TOKEN:dubbo-token-2026}
  # 消费者全局配置
  consumer:
    # 微服务最佳实践：启动时不检查提供者是否存在，消费者正常启动成功，等到运行时真正调用接口再去连接提供者
    check: ${DUBBO_CONSUMER_CHECK:false}
    # 消费者全局超时（优先级低于服务单独配置）
    # 限制等待响应的最长时间（从发起请求到接收响应）
    # 超时未收到响应则主动中断等待，抛出超时异常
    timeout: ${DUBBO_CONSUMER_TIMEOUT:10000}
    # 请求失败的重试次数（非幂等写操作必须 0，读操作可 1-2）
    retries: ${DUBBO_CONSUMER_RETRIES:0}
    # 线程池类型（cached, fixed, limit, eager）
    threadpool: ${DUBBO_CONSUMER_THREADPOOL:limit}
    # 核心线程数，必须非fixed才会识别corethreads
    corethreads: ${DUBBO_CONSUMER_CORE_THREADS:20}
    # 线程池最大线程数
    threads: ${DUBBO_CONSUMER_THREADS:60}
    # 响应等待队列
    queues: ${DUBBO_CONSUMER_QUEUES:30}
    # 负载均衡策略（random/roundrobin/leastactive）
    loadbalance: ${DUBBO_CONSUMER_LOADBALANCE:leastactive}
    parameters:
      # Dubbo安全令牌（防止未授权访问）- 使用固定token
      token: ${DUBBO_TOKEN:dubbo-token-2026}
      
nacos:
  # 配置中心专用配置
  config:
    bootstrap:
      # 启用预加载（核心配置）
      enable: true
      # 核心：打印 Bootstrap 阶段详细日志，必须启用 Bootstrap 模式，logEnable 才生效
      logEnable: true
    server-addr: ${NACOS_ADDRESS:127.0.0.1:8848}
    # Nacos 用户名（若开启认证）
    username: ${NACOS_USERNAME:nacos}
    # Nacos 密码（若开启认证）
    password: ${NACOS_PWD:nacos}
    # 兼容 Nacos 2.x 鉴权的备用参数
    accessKey: ${NACOS_USERNAME:nacos}
    # 兼容 Nacos 2.x 鉴权的备用参数
    secretKey: ${NACOS_PWD:nacos}
    # 命名空间ID
    namespace: ${NACOS_NAMESPACE:}
    # 配置组
    group: ${NACOS_GROUP:xx-platform}
    # Nacos配置Data ID
    data-id: xxx-xxx.yml
    #后缀名，只支持properties和yaml类型
    type: yaml
    #启用远程同步配置
    enable-remote-sync-config: true
    #开启nacos自动刷新
    auto-refresh: true
  # 注册中心专用配置，供自定义Nacos注册器使用
  discovery:
    server-addr: ${NACOS_ADDRESS:127.0.0.1:8848}
    # 注册到Nacos的服务名
    service-name: xx-service
    group: ${NACOS_GROUP:xx-platform}
    namespace: ${NACOS_NAMESPACE:}
    # Nacos 用户名（若开启认证）
    username: ${NACOS_USERNAME:nacos}
    # Nacos 密码（若开启认证）
    password: ${NACOS_PWD:nacos}
    
bigbird:
  server:
    rpc:
      core:
        # kryo注册类所在的包列表
        scanKryoSerializablePackages: com.a.xxx,com.b.xxx,com.c.xxx 
```

## 构件依赖

日志组件、SERVER核心能力构件。
