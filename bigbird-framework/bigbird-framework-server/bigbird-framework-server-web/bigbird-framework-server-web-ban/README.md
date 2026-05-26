# WEB构件禁用词模块

本构件属于统一WEB框架的禁用词模块，提供以下功能：

1、提供基于Redis的禁用词库管理。

2、基于Redis的Pub/Sub机制实现禁用词动态更新。

## 配置

本构件有以下重要配置，描述如下：

```
bigbird:
  server:
    web:
      ban:
        forbidWordPoolKey: word:forbid #禁用词库在redis中的键
        forbidWordRefreshEventTopic: word:forbid:refresh:topic #禁用词变更事件在redis中的发布渠道
```

## 构件依赖

日志组件、禁用词检测组件、Redis构件。
