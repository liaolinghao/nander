# SERVER通用能力构件调度模块

本构件属于统一SERVER通用能力的任务调度模块，提供以下功能：

1、提供任务调度服务，可用于各种定时任务管理场景。

2、需要在项目的resources目录中放置quartz.properties配置文件，并修改其中的调度策略。

## 任务调度服务

```
// 任务调度服务
@Autowired
private ITaskSchedulingService taskSchedulingService;
```

## 构件依赖

日志组件、MybatisPlus构件、SERVER核心能力构件。
