# WEB构件验证码模块

本构件属于统一WEB框架的验证码模块，提供以下功能：

1、提供验证码检查，可用于登录验证，修改重要信息等需要验证码的场景。

2、提供了WEB服务常用的验证码相关的REST功能接口。

## 配置

本构件配置描述如下：

### 用于设置验证码

```
bigbird:
  server:
    web:
      captcha:
        async: false # 是否异步发送
        backdoor-enable: true # 启用后门
        backdoor: xxx # 后门验证码
        backdoor-limit: 10 # 后门验证码每日可用次数
        verifyMax: 3 # 最大重复验证次数
        verify-time-period: 10 # 验证时间周期，秒为单位，在该周期内验证失败次数超过最大次数，就让验证码失效
        ttl: 30 # 验证码有效期，秒为单位
        length: 4  # 验证码长度
        width: 110  # 图片验证码宽度
        height: 40  # 图片验证码高度
        max-day-counts: 30  # 一天内同一个手机号在同一个场景获取短信验证码的最大次数
        max-hour-counts: 10  # 一小时内同一个手机号在同一个场景获取短信验证码的最大次数
        max-minute-counts: 1  # 一分钟内同一个手机号在同一个场景获取短信验证码的最大次数
        sms-frequency-max-count: 10000 # 短信频率控制：最大条数
        sms-frequency-duration-time: 600 # 短信频率控制：时长（秒）
        smsSceneTemplateIdMap: # 短信使用场景与模板ID对应关系
          login: 558529
          modify_phone: 558530
        emailSceneTemplateMap: # 邮箱使用场景与模板消息对应关系
          # 模版消息中的变量用{}标识，各分段用|分隔，第一段为邮件标题
          login: 登录验证|您好！|您的验证码是：{}|您可以复制此验证码进行验证。|此验证码只能使用一次，在{}分钟内有效。验证成功则自动失效。|如果您没有进行上述操作，请忽略此邮件。
          modify_email: 修改电子邮箱验证|您好！|您的验证码是：{}|您可以复制此验证码进行验证。|此验证码只能使用一次，在{}分钟内有效。验证成功则自动失效。|如果您没有进行上述操作，请忽略此邮件。
        slider:
          deviation: 5 # 滑块校验允许误差（默认是5）
          imageFileDir: # 背景图路径（默认是jar中图片）
          imageFormat: png # 背景图文件后缀（默认是png）
```

## 验证码服务

```
// 验证码服务
@Autowired
private ICaptchaService captchaService;
```

## 在核心模块基础之上新增常用接口

#### 获取图像验证码接口

<table>
<tr>
<td>地址</td>
<td colspan="5">/captcha/image/{scene}</td>
</tr>
<tr>
<td>请求方式</td>
<td colspan="5">GET</td>
</tr>
<tr>
<td></td>
<td>参数名称</td>
<td>参数值</td>
<td>必选</td>
<td>类型</td>
<td>描述</td>
</tr>
<tr>
<td>请求头</td>
<td>Session-Id</td>
<td></td>
<td>否</td>
<td>String</td>
<td>类似HttpSessionId，对于无法持有HttpSession的调用端，需要自行构思一种机制传递该值</td>
</tr>
<tr>
<td>请求参数</td>
<td>scene</td>
<td></td>
<td>是</td>
<td>String</td>
<td>验证码使用场景，取值为以下值之一：login，modify_phone</td>
</tr>
<tr>
<td>响应</td>
<td colspan="5">
图片流
</td>
</tr>
</table>

#### 获取滑块验证码接口

<table>
<tr>
<td>地址</td>
<td colspan="5">/captcha/slider/{scene}</td>
</tr>
<tr>
<td>请求方式</td>
<td colspan="5">GET</td>
</tr>
<tr>
<td></td>
<td>参数名称</td>
<td>参数值</td>
<td>必选</td>
<td>类型</td>
<td>描述</td>
</tr>
<tr>
<td>请求头</td>
<td>Session-Id</td>
<td></td>
<td>否</td>
<td>String</td>
<td>类似HttpSessionId，对于无法持有HttpSession的调用端，需要自行构思一种机制传递该值</td>
</tr>
<tr>
<td>请求参数</td>
<td>scene</td>
<td></td>
<td>是</td>
<td>String</td>
<td>验证码使用场景，取值为以下值之一：login，modify_phone</td>
</tr>
<tr>
<td>响应</td>
<td colspan="5">
<pre>
{
  "code": 200,
  "msg": "成功",
  "data": {
    "baseImg": "滑块底图BASE64字符串", 
    "patchImg": "滑块补丁BASE64字符串",
    "y": "Integer,y坐标"
  }
}
</pre>
</td>
</tr>
</table>

#### 验证滑块验证码接口

<table>
<tr>
<td>地址</td>
<td colspan="5">/captcha/slider/{scene}/check</td>
</tr>
<tr>
<td>请求方式</td>
<td colspan="5">POST</td>
</tr>
<tr>
<td></td>
<td>参数名称</td>
<td>参数值</td>
<td>必选</td>
<td>类型</td>
<td>描述</td>
</tr>
<tr>
<td>请求头</td>
<td>Session-Id</td>
<td></td>
<td>否</td>
<td>String</td>
<td>类似HttpSessionId，对于无法持有HttpSession的调用端，需要自行构思一种机制传递该值</td>
</tr>
<tr>
<td rowspan="2">请求参数</td>
<td>scene</td>
<td></td>
<td>是</td>
<td>String</td>
<td>验证码使用场景，取值为以下值之一：login，modify_phone</td>
</tr>
<tr>
<td colspan="5">
<pre>
{
  "id": "滑块验证码ID",
  "x": "Double,水平偏移值"
}
</pre>
</td>
</tr>
<tr>
<td>响应</td>
<td colspan="5">
<pre>
{
  "code": 200,
  "msg": "成功",
  "data": "验证码值"
}
</pre>
</td>
</tr>
</table>

#### 获取短信验证码接口

<table>
<tr>
<td>地址</td>
<td colspan="5">/captcha/sms</td>
</tr>
<tr>
<td>请求方式</td>
<td colspan="5">POST</td>
</tr>
<tr>
<td></td>
<td>参数名称</td>
<td>参数值</td>
<td>必选</td>
<td>类型</td>
<td>描述</td>
</tr>
<tr>
<td rowspan="2">请求头</td>
<td>Content-Type</td>
<td>application/json</td>
<td>是</td>
<td>String</td>
<td></td>
</tr>
<tr>
<td>Session-Id</td>
<td></td>
<td>否</td>
<td>String</td>
<td>类似HttpSessionId，对于无法持有HttpSession的调用端，需要自行构思一种机制传递该值</td>
</tr>
<tr>
<td>请求参数</td>
<td colspan="5">
<pre>
{
  "mobilephone": "xxx", // 手机号，加密传输
  "captchaType": "IMAGE/SLIDER", // 验证码类型
  "captchaValue": "xxx", // 验证码值
  "scene": "xxx", // 验证码使用场景，取值为以下值之一：login，modify_phone
  "ignoreScene": "true/false，表示频率控制是否忽略场景，默认为true"
}
</pre>
</td>
</tr>
<tr>
<td>响应</td>
<td colspan="5">
<pre>
{
  "code": 200,
  "msg": "成功"
}
</pre>
</td>
</tr>
</table>

#### 获取邮箱验证码接口

<table>
<tr>
<td>地址</td>
<td colspan="5">/captcha/email</td>
</tr>
<tr>
<td>请求方式</td>
<td colspan="5">POST</td>
</tr>
<tr>
<td></td>
<td>参数名称</td>
<td>参数值</td>
<td>必选</td>
<td>类型</td>
<td>描述</td>
</tr>
<tr>
<td rowspan="2">请求头</td>
<td>Content-Type</td>
<td>application/json</td>
<td>是</td>
<td>String</td>
<td></td>
</tr>
<tr>
<td>Session-Id</td>
<td></td>
<td>否</td>
<td>String</td>
<td>类似HttpSessionId，对于无法持有HttpSession的调用端，需要自行构思一种机制传递该值</td>
</tr>
<tr>
<td>请求参数</td>
<td colspan="5">
<pre>
{
  "email": "xxx", // 电子邮箱，加密传输
  "captchaType": "IMAGE/SLIDER", // 验证码类型
  "captchaValue": "xxx", // 验证码值
  "scene": "xxx", // 验证码使用场景，取值为以下值之一：login，modify_email
  "ignoreScene": "true/false，表示频率控制是否忽略场景，默认为true"
}
</pre>
</td>
</tr>
<tr>
<td>响应</td>
<td colspan="5">
<pre>
{
  "code": 200,
  "msg": "成功"
}
</pre>
</td>
</tr>
</table>

## 构件依赖

日志组件、Redis构件、消息构件。
