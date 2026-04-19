# Elasticsearch构件

本构件用于管理Elasticsearch相关的数据持久化操作。

```
// 索引服务
@Autowired
private EsIndexService esIndexService;

// 批量操作服务
@Autowired
private EsBulkService esBulkService;

// 查询服务
@Autowired
private EsQueryService esQueryService;

// 数据操作服务：添加，更新，删除数据
@Autowired
private EsManipulateService esManipulateService;
```

## 配置

本构件自定义elasticsearch配置，相比较spring原生的elasticsearch配置，多定义了：socket超时时间，获取请求等待超时时间等属性配置。
同时，本构件兼容spring原生的elasticsearch配置。

配置加载优先级如下：

1、自定义配置优先

2、spring原生配置作为候补

具体描述如下：

```
# elasticsearchTemplate定义存在冲突，需要开启以下配置
spring:
  main:
    allow-bean-definition-overriding: true

bigbird:
  data:
    elasticsearch:
      key: # 加解密密钥
      scheme: # 默认为http，使用searchguard后为https
      addresses: # 连接地址，逗号分隔
      username: # 用户名
      password: # 密码
      truststorePassword: # searchguard:truststore.jks的生成密码
      truststorePath: # searchguard:truststore.jks的路径
      connectTimeout: # 连接超时时间，单位：毫秒
      socketTimeout: # socket超时时间，单位：毫秒
      connectionRequestTimeout: # 从连接池获取连接超时时间，单位：毫秒
      maxConnectNum: # 最大连接数
      maxConnectPerRoute: # 最大路由连接数
      maxRetryTimeout: # 最大重试超时时间，单位：毫秒
      bulkActions: # 每添加几个request，执行一次bulk操作
      bulkSize: # 达到几M的请求大小时，执行一次bulk操作
      bulkFlushInterval: # 每几秒执行一次bulk操作
      bulkConcurrentRequests: # bulk的并发线程数
```
