# 分布式锁构件

本构件用于对指定方法加分布式锁，提供如下两种实现：

基于REDIS的分布式锁：锁的模型不够健壮，宕机发生时，存在丢失数据的可能性，导致分布式锁出现问题。
同时，获取不到锁时会直接不断尝试获取锁，比较消耗性能。但好处是性能高，支持获取锁时设置等待时间。

基于ZK的分布式锁：锁的模型健壮、简单易用、如果获取不到锁，只需要添加一个监听器就可以了，不用一直轮询，
性能消耗较小。

使用时，服务可选择依赖 bigbird-framework-distributedlock-redislock或者
bigbird-framework-distributedlock-zklock两者中的一个。同时，在对应方法上加入注解@Lock即可。

```
@Lock(lockKey="锁键（字符串或spel表达式）",
lockKeyPrefix="锁键前缀",
leaseTime="持有时长,小于等于0永久持有锁",
timeUnit="时间单位,默认秒")
```

## 构件依赖

Redis构件、Zookeeper构件。
