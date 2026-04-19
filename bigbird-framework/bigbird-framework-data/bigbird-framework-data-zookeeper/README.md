# Zookeeper构件

本构件用于管理Zookeeper相关的数据持久化操作。

```
// zookeeper 基础服务
@Autowired
private ZookeeperService zookeeperService;
```

## 配置

本构件自定义zookeeper配置。

具体描述如下：

```
bigbird:
  data:
    zookeeper:
      addresses:  # 节点地址，逗号分隔
      namespace: # 命名空间
      sessionTimeout: # 会话超时时间，单位：毫秒，默认5秒
      connectTimeout: # 连接超时，单位：毫秒，默认5秒
      retry:
        type: # 重连策略，枚举值，默认以指数级延迟的重连模式
        retryTime: # 重连间隔时间，以秒为单位，默认1秒
        maxSleepTime: # 最大重连间隔时间，以秒为单位，默认30秒
        maxRetries: # 最大重连次数，默认10次
        retryUntilElapsed: # 总等待时间，以秒为单位，默认10秒
      authentication:
        key: # 加解密密钥
        type: # 认证策略，枚举值，默认任何客户端都可以访问
        username: # 认证用户名
        password: # 认证密码
```
