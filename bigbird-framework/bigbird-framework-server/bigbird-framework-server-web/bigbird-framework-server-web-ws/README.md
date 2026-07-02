# WEB构件WS模块

本构件属于统一WEB框架的WS模块，提供以下功能：

1、提供支持全双工透传的客户端与服务端。

2、提供支持建立连接时进行Token鉴权。

3、集成了Tomcat WS和Spring WS两种WS运行机制（二选一，优先选用Spring WS），运行中通过开关springEnabled决定采用哪种机制。

备注：本构件实现了一套WS连接时进行鉴权的处理流程，同时提供了两个特殊的服务接口，用于支持权限验证处理。具体说明如下：

ITokenService：如果服务存在自己独有的鉴权方式，可通过提供实现该接口的服务组件来完成鉴权。

ITargetWsAuthService：如果目标WS服务存在自己独有的赋权方式，可通过提供实现该接口的服务组件来完成赋权。

IDataProcessService：如果服务存在自己独有的数据加工方式，可通过提供实现该接口的服务组件来完成数据加工。

## 配置

本构件有以下重要配置，描述如下：

```
bigbird:
  server:
    web:
      ws:
        springEnabled: true # 是否开启Spring WS机制，该机制更灵活，性能更好
        target: ws://ip:port # 目标ws
        relayPath: /v1/** # Spring WS机制透传ws路径模式，匹配所有/v1开头的路径
        allowedOrigins: ["*"] # Spring WS机制允许跨域的域名，列表
```

## Tomcat WS机制服务实现方式

```
// 每个路径都需要写一个空壳子类
@Component
@ServerEndpoint(value = "/v1/asr_realtime", configurator = WsHandshakeConfiguration.class)
public class AsrWsRelayServer extends AbstractWsRelayServer {

}
```

## 构件依赖

日志组件。
