# 日志组件核心模块

本组件属于统一日志框架的核心模块，提供基础日志与自定义日志操作。

## 配置

### 用于记录自定义模块的性能日志

```
bigbird:
  common:
    logging:
      # 自定义日志属性配置，针对SpringBoot里通过自定义注解@Logging标注的方法做日志
      custom: 
        enable: true # 是否开启 
        level: info # 支持 trace、debug、info、warn、error  
        serialize-length: 2048 # 序列化长度，设置合理的长度可以防止打印日志过长使JVM内存溢出
```
