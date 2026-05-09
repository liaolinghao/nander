# SERVER通用能力构件频控模块

本构件属于统一SERVER通用能力的频控模块，提供以下功能：

1、提供频率检查，可用于各种频率控制场景。

## 频控服务

```
// 频控服务
@Autowired
private IFrequencyCheckerService frequencyCheckerService;
```

## 构件依赖

日志组件、Redis构件。
