# Redis构件

本构件用于管理Redis相关的数据持久化操作。

```
// redis 基础服务
@Autowired
private RedisService redisService;

// redis set 服务
@Autowired
private RedisSetService redisSetService;

// redis 有序set 服务
@Autowired
private RedisSortedSetService redisSortedSetService;

// redis hash 服务
@Autowired
private RedisHashService redisHashService;

// redis list 服务
@Autowired
private RedisListService redisListService;
```

## 配置

本构件自定义redis配置，相比较spring原生的redis配置，多定义了：命令等待超时时间。
同时，本构件兼容spring原生的redis配置。

配置加载优先级如下：

1、自定义配置优先

2、spring原生配置作为候补

具体描述如下：

```
bigbird:
  data:
    redis:
      key: # 加解密密钥
      addresses:  #节点地址，逗号分隔
      password: #密码
      database: #库编号（单机版可用）
      timeout: #命令等待超时，单位：毫秒
      connectTimeout: #连接超时，单位：毫秒
      connectionPoolSize: #节点连接池大小
      connectionMinimumIdleSize: #节点最小空闲连接数
```
