# 日志组件Cloud模块

本组件属于统一日志框架的Cloud模块，在Web模块功能之上额外提供FeignClient模块的性能日志操作。

## 配置

### 用于记录微服务中FeignClient模块的性能日志

```
bigbird:
  common:
    logging:
      # Feign日志属性配置，针对SpringCloud里Feign中的远程调用接口做日志
      feign:  
        # 是否开启
        enable: true  
        # 支持 trace、debug、info、warn、error
        level: info  
        # 序列化长度，设置合理的长度可以防止打印日志过长使JVM内存溢出
        serialize-length: 100 
        # 排除的类或方法
        excludes: 
```
