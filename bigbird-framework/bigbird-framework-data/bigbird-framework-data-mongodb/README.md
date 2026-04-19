# Mongodb构件

本构件用于管理Mongodb相关的数据持久化操作。

```
// 查询服务
@Autowired
private MongoDBQueryService mongoDBQueryService;

// 数据操作服务：添加，更新，删除数据
@Autowired
private MongoDBManipulateService mongoDBManipulateService;

// 索引服务
@Autowired
private MongoDBIndexService mongoDBIndexService;

// 聚合服务：求和
@Autowired
private MongoDBAggService mongoDBAggService;
```

## 配置

本构件自定义mongodb配置，相比较spring原生的mongodb配置，多定义了：超时时间，连接池等属性配置。
同时，本构件兼容spring原生的mongodb配置。

配置加载优先级如下：

1、自定义配置优先

2、spring原生配置作为候补

具体描述如下：

```
# mongoTemplate定义存在冲突，需要开启以下配置
spring:
  main:
    allow-bean-definition-overriding: true

bigbird:
  data:
    mongodb:
      key: # 加解密密钥
      uri:  # 实例uri，必须配置
      database: # 数据库名称，必须配置
      username: # 用户名
      password: # 密码
      readTimeout: # 读超时时间，单位：毫秒，默认不超时
      connectTimeout: # 连接超时时间，单位：毫秒，默认10秒
      connectionPoolMinSize: # 连接池空闲时保持的最小连接数，默认5
      connectionPoolMaxSize: # 连接池允许的最大连接数，默认50
      connectionPoolMaxWaitTime: # 线程等待连接变为可用的最长时间，单位：毫秒，值为0意味着它不会等待，负值意味着它将无限期地等待，默认3分钟
```
