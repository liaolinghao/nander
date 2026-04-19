# 缓存构件

本构件基于Spring Cache提供两种模式的可根据缓存名称自定义缓存数据过期时间的缓存实现。

|  时间表达式   | 解释  |
|  ----  | ----  |
| 1D或者1d  | 1天 |
| 1H或者1h  | 1小时 |
| 1M或者1m  | 1分钟 |
| 1S或者1s  | 1秒 |
| 1  | 1毫秒 |

以用户缓存UserCache为例，具体说明如下：

1、缓存名称：UserCache

缓存的过期时间以设置的全局缓存时间为准。

2、缓存名称：UserCache%%ttl时间表达式

缓存的过期时间以设置的ttl时间表达式对应的缓存时间为准。

3、缓存名称：UserCache%%ttl时间表达式%%maxIdle时间表达式

缓存的过期时间以设置的ttl时间表达式对应的缓存时间为准，同时可以根据maxIdle时间表达式定义缓存数据的最长访问周期。

4、缓存名称可以设置固定值也可以根据传入参数动态构造，如：
cacheNames = "'test:cache%%' + #ttl"，在方法中，ttl参数传值5m，此时缓存名称为：test:cache%%5m。

## 基于Redission的分布式缓存

该方式依赖Redis构件，可通过缓存名称自定义缓存数据过期时间，具体描述如下：

```
bigbird:
  data:
    redis:
      addresses: # 节点地址，逗号分隔
      password: # 密码
      database: # 库编号（单机版可用）
      timeout: # 命令等待超时，单位：毫秒
      connectTimeout: # 连接超时，单位：毫秒
      connectionPoolSize: # 节点连接池大小
      connectionMinimumIdleSize: # 节点最小空闲连接数
      
spring:
  cache:
    enable: true
    type: redis
    redis:
      # 过期时间
      # "PT20.345S" -- parses as "20.345 seconds"
      # "PT15M"     -- parses as "15 minutes" (where a minute is 60 seconds)
      # "PT10H"     -- parses as "10 hours" (where an hour is 3600 seconds)
      # "P2D"       -- parses as "2 days" (where a day is 24 hours or 86400 seconds)
      # "P2DT3H4M"  -- parses as "2 days, 3 hours and 4 minutes"
      # "PT-6H3M"    -- parses as "-6 hours and +3 minutes"
      # "-PT6H3M"    -- parses as "-6 hours and -3 minutes"
      # "-PT-6H+3M"  -- parses as "+6 hours and -3 minutes"
      timeToLive: PT1H
      # 确定两次请求对象之间可以经过的最长时间，不设置代表不限制
      maxIdleTime: PT5S
```

## 基于Caffeine的本地缓存

```
spring:
  cache:
    enable: true
    type: caffeine
    caffeine:
      # 过期时间
      # "PT20.345S" -- parses as "20.345 seconds"
      # "PT15M"     -- parses as "15 minutes" (where a minute is 60 seconds)
      # "PT10H"     -- parses as "10 hours" (where an hour is 3600 seconds)
      # "P2D"       -- parses as "2 days" (where a day is 24 hours or 86400 seconds)
      # "P2DT3H4M"  -- parses as "2 days, 3 hours and 4 minutes"
      # "PT-6H3M"    -- parses as "-6 hours and +3 minutes"
      # "-PT6H3M"    -- parses as "-6 hours and -3 minutes"
      # "-PT-6H+3M"  -- parses as "+6 hours and -3 minutes"
      timeToLive: PT1H
      # 设置该项，则以该设置为准构造caffeine缓存
      spec: maximumSize=200,expireAfterWrite=300s,recordStats
```

## 构件依赖

Redis构件。
