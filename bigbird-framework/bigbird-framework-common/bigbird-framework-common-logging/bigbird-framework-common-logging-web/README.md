# 日志组件Web模块

本组件属于统一日志框架的Web模块，在核心模块功能之上额外提供Controller模块的性能日志操作。

## 配置

### 用于记录微服务中Controller模块的性能日志

```
bigbird:
  common:
    logging:
      # Controller日志属性配置，针对SpringBoot里Controller中的Rest接口做日志
      controller:  
        enable: true # 是否开启 
        level: info # 支持 trace、debug、info、warn、error 
        serialize-length: 100 # 序列化长度，设置合理的长度可以防止打印日志过长使JVM内存溢出
        excludes: # 排除的类或方法
```
