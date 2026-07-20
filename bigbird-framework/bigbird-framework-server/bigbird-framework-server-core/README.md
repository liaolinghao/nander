# SERVER核心能力构件

本构件用于统一SERVER核心能力，提供以下功能：

1、约定了统一异常响应（见IBaseResponseStatus）。

2、提供了用于性能分析的时间追踪器（见TimeTracer）。

3、提供了实现自定义签名算法的接口签名工具类（见SignatureUtils）。

## 配置

本构件有以下重要配置，描述如下：

```
bigbird:
  server:
    core:
      tracer:
        enable: # 是否启用链路性能日志，true-开启，false-关闭
        threshold: 
          ms: # 是否打印性能分析完整日志的时间阀值，毫秒为单位，默认300毫秒
```

## 构件依赖

日志组件。
