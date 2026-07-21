# SERVER统一WEB能力构件网关模块

本构件属于SERVER统一WEB能力的网关模块，提供以下功能：

1、专用于对外提供REST接口的服务集成神禹网关能力使用。

2、集成Nacos为注册中心。

## 配置

本构件有以下重要配置，描述如下：

```
spring:
  cloud:
    discovery:
      enabled: true
    nacos:
      discovery:
        enabled: true
        # Nacos 注册中心地址（集群用逗号分隔：127.0.0.1:8848,127.0.0.1:8849）
        server-addr: ${NACOS_ADDRESS:127.0.0.1:8848}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PWD:nacos}
        namespace: ${NACOS_SHENYU_NAMESPACE:}
        group: ${NACOS_SHENYU_GROUP:DEFAULT_GROUP}

server:
  port: ${WEB_PORT:8080}
  tomcat:
    # 设置连接超时，以防止缓慢的HTTP拒绝服务攻击
    connection-timeout: ${CONNECTION_TIMEOUT:10000}
  # 设置默认UTF-8编码返回响应
  servlet:
    encoding:
      charset: UTF-8
      force: true
      enabled: true
  # 设置Gzip压缩，提高网络传输速度
  compression:
    enabled: true
    mime-types: application/javascript,text/css,application/json,application/xml,text/html,text/xml,text/plain

shenyu:
  register:
    # 经过实践，目前仅有http注册模式能正常运行
    # http #zookeeper #etcd #nacos #consul
    registerType: ${SHENYU_REGISTER_TYPE:http}
    # http://localhost:9095 #localhost:2181 #http://localhost:2379 #localhost:8848
    serverLists: ${SHENYU_REGISTER_SERVER:http://localhost:9095}
    props:
      username: ${SHENYU_REGISTER_USERNAME:nacos}
      password: ${SHENYU_REGISTER_PWD:nacos}
      namespace: ${NACOS_SHENYU_NAMESPACE:}
      group: ${NACOS_SHENYU_GROUP:DEFAULT_GROUP}
  client:
    springCloud:
      props:
        # 服务上下文路径，替代了server.servlet.context-path
        # server.servlet.context-path不需要再配置
        contextPath: /xx/xxx
        # 服务端口（自动读取 server.port）
        port: ${server.port}
        # 是否自动注册所有接口
        isFull: true

# 暴露健康检查端点（网关需要检测服务是否存活）
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
      
nacos:
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
    group: ${NACOS_GROUP:xxx-xxx}
    # Nacos配置Data ID
    data-id: xxx-xxx.yml
    #后缀名，只支持properties和yaml类型
    type: yaml
    #启用远程同步配置
    enable-remote-sync-config: true
    #开启nacos自动刷新
    auto-refresh: true
```

## 构件依赖

日志组件、SERVER核心能力构件。
