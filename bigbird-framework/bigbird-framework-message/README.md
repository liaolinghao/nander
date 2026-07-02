# 消息通知构件

本构件用于发送实时消息通知，提供如下四种实现：

基于钉钉的实时消息通知：优点：免费，实时性高；缺点：要求用户安装钉钉，并且加好友或者加入指定组织。

基于企业微信的实时消息通知：优点：免费，实时性高；缺点：要求用户安装企业微信，并且加好友或者加入指定组织。

基于短信的实时消息通知：优点：实时性高；缺点：大规模消息产生的费用高。

基于邮件的实时消息通知：优点：可发送大文本内容，费用低，接近免费；缺点：实时性差。

# 消息发送公共配置

```
bigbird:
  message:
    frequency: 10 # 消息发送的间隔频率，毫秒为单位
```

# 邮件消息

```
spring:
  mail:
    default-encoding: utf-8
    # 开启ssl时使用smtps，否则使用smtp
    protocol: smtp
    # 邮箱服务器
    host: smtp.qq.com 
    # 服务端口
    port: 587 
    # 认证用户名
    username: 26089183@qq.com 
    # 授权密码
    password: mail4Bird 
```

# 钉钉消息

```

```

# 企业微信消息

```
bigbird:
  message:
    wechat:
      corpId: # 企业ID
      secret: # 企业密钥
      agentId: # 应用ID
      baseUrl: # 企业微信API域名地址
      expires: # token有效期，秒为单位
      
retrofit:
  enable-response-call-adapter: true
  # 启用日志打印
  enable-log: true
  # 是否启用熔断降级
  enable-degrade: true
  # 连接池配置
  pool:
    test1:
      max-idle-connections: 3
      keep-alive-second: 100
    test2:
      max-idle-connections: 5
      keep-alive-second: 50
```

# 短信消息

```
bigbird:
  message:
    sms:
      type: open或者integrated # open代表能力开放平台，integrated代表一体化服务平台
      open: # 当类型为open时配置
        token:  # 访问token
        appId: # 在短信平台注册的应用ID
        baseUrl: # 短信服务API域名地址
      integrated: # 当类型为integrated时配置
        cpCode: # 渠道在一体化消息服务平台申请的cpCode
        accessKey: # 一体化消息服务平台分配的私钥
        baseUrl: # 短信服务API域名地址
      
retrofit:
  enable-response-call-adapter: true
  # 启用日志打印
  enable-log: true
  # 是否启用熔断降级
  enable-degrade: true
  # 连接池配置
  pool:
    test1:
      max-idle-connections: 3
      keep-alive-second: 100
    test2:
      max-idle-connections: 5
      keep-alive-second: 50
```
