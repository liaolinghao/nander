# SERVER统一WEB能力构件核心模块

本构件属于SERVER统一WEB能力的核心模块，提供以下功能：

1、约定了时间为null的判断机制、线程池策略、跨域访问策略、Swagger配置和密码验证策略配置（见PasswordPolicy，弱密码库要求采用全小写录入）。

2、提供了WEB服务常用的REST功能接口。

3、提供对后台服务长时间处理任务的状态控制（对于存在此类任务的控制器建议继承CommonController以实现状态修改）。

4、提供了传输内容加解密机制，支持字段级加解密，数据包级加解密及其相关工具与多种实现策略。

## 加解密

### 字段级

```
1、对于加密传输的@RequestParam类型的接口参数，可用@DecryptRequestParam替换@RequestParam实现自动解密

2、对于加密传输的@PathVariable类型的接口参数，可用@DecryptPathVariable替换@PathVariable实现自动解密

3、对于加密传输的@RequestBody类型的接口参数，可用@DecryptField注解实现自动解密（需要搭配@Decrypt(all = false)）

4、对于响应字段可配置@JsonSerialize(using = XxxSerializer.class)实现字段级加密返回，序列化支撑类在support包下。
```

### 数据包级

```
1、在Controller类对应方法上添加注解@Decrypt可实现对@RequestBody注解的参数进行解密，
@Decrypt(all = false)表示对@DecryptField字段解密，@Decrypt(all = true)表示整体解密

2、在Controller类对应方法上添加注解@Encrypt可实现对@ResponseBody注解的方法响应或@RestController注解的类中方法响应进行整体加密。
```

### 工具

```
/**
 * Rest请求加密解密拦截器
 * 基于Spring Cloud的微服务在使用RestTemplate调用的时候，
 * 可通过注入该拦截器实现对请求数据的自动加密解密处理。
 */
@Bean
@LoadBalanced
RestTemplate restTemplate(){
    RestTemplate restTemplate = new RestTemplate();
    CryptoRestInterceptor cryptoRestInterceptor = new CryptoRestInterceptor();
    cryptoRestInterceptor.setEncryptService(encryptService);
    cryptoRestInterceptor.setEncryptKey(encryptKey);
    cryptoRestInterceptor.setDecryptService(decryptService);
    cryptoRestInterceptor.setDecryptKey(decryptKey);
    cryptoRestInterceptor.setIv(iv);
    restTemplate.setInterceptors(Collections.singletonList(cryptoRestInterceptor));
    return restTemplate;
}
```

## 配置

本构件有六类重要配置，分别描述如下：

### 参数的加解密密钥与静态目录映射

```
bigbird:
  server:
    web:
      core:
        crypto: # 加解密器类型，可选值：SIMPLE，AES，RSA，SM4，ENVELOPE
        enableEncrypt: # 是否启动接口响应加密
        encryptKey: # 加解密器采用的加密密钥，对于非对称密钥算法，可配置公钥
        enableDecrypt: # 是否启动接口请求解密
        decryptKey: # 加解密器采用的解密密钥，对于非对称密钥算法，可配置私钥
        dir: # 业务数据目录，默认采用static，但是实际部署时建议区分目录，将static作为配置文件目录，业务数据存储改用其他目录
        pattern: # 静态目录映射关系配置，key为url模式（/ctwp/**）中的标识，value为目录绝对路径
          ctwp: file:E:/idea/bigbird/bigbird-domain/ctwp/
```

### 用于线程池策略

```
bigbird:
  server:
    web:
      core:
        pool: 
          maxPoolSize: 50 # 最大线程数量，默认50
          corePoolSize: 20 # 核心线程数量，默认20
          queueCapacity: 100000 # 队列最大长度
          keepAliveSeconds: 60 # 线程存活时间，秒为单位
          threadNamePrefix: async-task- # 线程前缀
          policy: ABORT # 任务量数量超过限制后的拒绝策略，可选值：ABORT，CALLER_RUNS，DISCARD，DISCARD_OLDEST
```

### 用于JSON转换时对时间为null的判断机制

```
bigbird:
  server:
    web:
      core:
        jackson:
          # web框架有关数据进行json格式转换时，对null值时间进行判断的表达式，比如时间格式化后为：1970-01-01 00:00:00，可以认为设置的时间为null
          setNullDateTime: 1970-01-01 00:00:00
          # 长整型是否转换为字符串开关，默认打开以解决前端JS对长整型支持不足的问题，对于RPC模式服务，需要关闭
          longToString: true
```

### 用于跨域访问策略

```
bigbird:
  server:
    web:
      core:
        cors:
          enable: true # 是否开启跨域访问策略
          allowedOrigins: ["*"] # 允许跨域的域名，列表
          allowedMethods: [GET,HEAD,POST,PUT,PATCH,DELETE,OPTIONS,TRACE] # 允许跨域的请求方法，列表
          allowedHeaders: ["*"] # 允许跨域携带的请求头，列表
          exposedHeaders: ["*"] # 允许跨域暴露的请求头，列表
          allowCredentials: true # 跨域请求也会带上cookie信息
          maxAge: 60 # 预检请求的有效期 
```

### 用于Swagger配置

```
springfox:
  documentation:
    enabled: false # 当swagger关闭时，建议增加这项配置，以便禁用页面，防止渗透扫描检测出漏洞

bigbird:
  server:
    web:
      core:
        swagger:
          enable: true # 是否开启swagger
          basePackage:  # 扫描包路径，可以不指定，系统会通过自动扫描{@link io.swagger.annotations.ApiOperation}
          title:  # swagger文档生成的标题
          description:  # 应用描述
          serviceUrl:  # API接口的服务地址，API接口的网址域名前缀
          version: # API版本号，默认V1.0.0
          license:  # 许可协议名称
          licenseUrl:  # 许可协议访问地址
          host: # swagger接口文档服务访问地址，默认为：服务启动入口地址/swagger-ui/index.html
```

### 用于自定义业务异常处理

自定义异常一般采用BusinessException进行抛出即可，如有需要，建议采用继承自BaseBusinessException 的派生异常进行抛出，这样可以通过如下配置进行统一处理。

```
bigbird:
  server:
    web:
      core:
        base-business-exception-handle:
          enable: true  # web框架开启对各类继承自BaseBusinessException异常的处理
```

## 常用接口

#### 健康状态检查接口

<table>
<tr>
<td>地址</td>
<td colspan="5">/health/check</td>
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
<td></td>
<td></td>
<td></td>
<td></td>
<td></td>
</tr>
<tr>
<td>请求参数</td>
<td></td>
<td></td>
<td></td>
<td></td>
<td></td>
</tr>
<tr>
<td>响应</td>
<td colspan="5">
<pre>
{
    "code": 200,
    "msg": "成功",
    "data": {
        "disk": {
            "C:\\": {
                "总空间": "271.4G",
                "可用空间": "199.3G",
                "空闲空间": "199.3G"
            },
            "D:\\": {
                "总空间": "205G",
                "可用空间": "194.5G",
                "空闲空间": "194.5G"
            }
        },
        "memory": {
            "最大可用内存": 4213178368,
            "已使用的内存": 43190176,
            "初始的总内存": 264241152,
            "JVM初始总内存": "252M",
            "JVM已使用的内存": "41.2M",
            "总的物理内存": "15.69G",
            "JVM最大可用内存": "4018M",
            "已使用的物理内存": "10.66G",
            "剩余的物理内存": "5.03G"
        },
        "cpu": {
            "cpu用户使用率": "0.84%",
            "cpu核数": 8,
            "cpu当前空闲率": "98.65%",
            "cpu当前等待率": "0%",
            "cpu系统使用率": "0.44%"
        },
        "status": "0"
    }
}
</pre>
</td>
</tr>
</table>

#### 配置项查看接口

<table>
<tr>
<td>地址</td>
<td colspan="5">/health/env/config</td>
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
<td></td>
<td></td>
<td></td>
<td></td>
<td></td>
</tr>
<tr>
<td>请求参数</td>
<td>key</td>
<td></td>
<td>是</td>
<td>String</td>
<td>配置键</td>
</tr>
<tr>
<td>响应</td>
<td colspan="5">
<pre>
{
    "code": 200,
    "msg": "成功",
    "data": "xxx"
}
</pre>
</td>
</tr>
</table>

#### 获取服务部署的机器时间戳信息接口

<table>
<tr>
<td>地址</td>
<td colspan="5">/common/load-server-time-stamp</td>
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
<td></td>
<td></td>
<td></td>
<td></td>
<td></td>
</tr>
<tr>
<td>请求参数</td>
<td></td>
<td></td>
<td></td>
<td></td>
<td></td>
</tr>
<tr>
<td>响应</td>
<td colspan="5">
<pre>
{
    "code": 200,
    "msg": "成功",
    "data": 1660112624227
}
</pre>
</td>
</tr>
</table>

#### 从Session中获取对应键值接口

<table>
<tr>
<td>地址</td>
<td colspan="5">/common/get-from-session/{key}</td>
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
<td></td>
<td></td>
<td></td>
<td></td>
<td></td>
</tr>
<tr>
<td>请求参数</td>
<td>key</td>
<td></td>
<td>是</td>
<td>String</td>
<td>缓存键</td>
</tr>
<tr>
<td>响应</td>
<td colspan="5">
<pre>
{
    "code": 200,
    "msg": "成功",
    "data": "A"
}
</pre>
</td>
</tr>
</table>

#### 将指定键值信息存入Session接口

<table>
<tr>
<td>地址</td>
<td colspan="5">/common/save-in-session</td>
</tr>
<tr>
<td>请求方式</td>
<td colspan="5">PUT</td>
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
<td>Content-Type</td>
<td>application/json</td>
<td>是</td>
<td>String</td>
<td></td>
</tr>
<tr>
<td>请求参数</td>
<td colspan="5">
<pre>
{
    "k1": "v1",
    "k2": "v2",
    "k3": "v3"
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

#### 从Session中移除指定键值接口

<table>
<tr>
<td>地址</td>
<td colspan="5">/common/remove-from-session/{keyInfo}</td>
</tr>
<tr>
<td>请求方式</td>
<td colspan="5">DELETE</td>
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
<td></td>
<td></td>
<td></td>
<td></td>
<td></td>
</tr>
<tr>
<td>请求参数</td>
<td>keyInfo</td>
<td></td>
<td>是</td>
<td>String</td>
<td>以,分隔的多个缓存键</td>
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

#### 设置指定处理ID对应的处理过程为取消状态接口

<table>
<tr>
<td>地址</td>
<td colspan="5">/common/cancel-process/{processId}</td>
</tr>
<tr>
<td>请求方式</td>
<td colspan="5">PUT</td>
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
<td></td>
<td></td>
<td></td>
<td></td>
<td></td>
</tr>
<tr>
<td>请求参数</td>
<td>processId</td>
<td></td>
<td>是</td>
<td>String</td>
<td>处理ID</td>
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

#### 获取指定处理ID对应的处理过程状态接口

<table>
<tr>
<td>地址</td>
<td colspan="5">/common/get-process/{processId}</td>
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
<td></td>
<td></td>
<td></td>
<td></td>
<td></td>
</tr>
<tr>
<td>请求参数</td>
<td>processId</td>
<td></td>
<td>是</td>
<td>String</td>
<td>处理ID</td>
</tr>
<tr>
<td>响应</td>
<td colspan="5">
<pre>
{
    "code": 200,
    "msg": "成功",
    "data":"xxx"
}
</pre>
</td>
</tr>
</table>

#### 上传文件接口

<table>
<tr>
<td>地址</td>
<td colspan="5">/common/file/upload?currentAttachName=xxx&supportSuffix=xxx</td>
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
<td>Content-Type</td>
<td>application/x-www-form-urlencoded</td>
<td>是</td>
<td>String</td>
<td></td>
</tr>
<tr>
<td rowspan="3">请求参数</td>
<td>currentAttachName</td>
<td></td>
<td>否</td>
<td>String</td>
<td>当前附件名称</td>
</tr>
<tr>
<td>supportSuffix</td>
<td></td>
<td>否</td>
<td>String</td>
<td>支持的文件格式，多种格式采用英文格式,分开，必须是支持的白名单中的格式子集</td>
</tr>
<tr>
<td>file</td>
<td></td>
<td>是</td>
<td>File</td>
<td>上传文件对象</td>
</tr>
<tr>
<td>响应</td>
<td colspan="5">
<pre>
{
   "code": 200,
   "msg": "成功",
   "data": {
       "attachPath": "static/temp/8d37c790c0324a239237c6a4930bdf40.png", // 图像文件地址
       "downloadPath":  "xxxxxx" // RC4加密文件路径，后续删除或者下载服务器上该文件都要传递该加密路径
       "fileName": "beetl.png", // 图片名称
       "width": 165, // 图片宽
       "height": 71 // 图片高
   }
}
</pre>
</td>
</tr>
<tr>
<td>备注</td>
<td colspan="5">
<pre>
白名单：
png,jpg,jpeg,gif,bmp,
flv,swf,mkv,avi,rm,rmvb,mpeg,mpg,
ogg,ogv,mov,wmv,mp4,webm,mp3,wav,mid,
rar,zip,tar,gz,7z,bz2,cab,iso,
doc,docx,xls,xlsx,ppt,pptx,pdf,txt,md,xml
</pre>
</td>
</tr>
</table>

#### 删除文件接口

<table>
<tr>
<td>地址</td>
<td colspan="5">/common/file/delete?filePath=xxx</td>
</tr>
<tr>
<td>请求方式</td>
<td colspan="5">DELETE</td>
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
<td></td>
<td></td>
<td></td>
<td></td>
<td></td>
</tr>
<tr>
<td>请求参数</td>
<td>filePath</td>
<td></td>
<td>是</td>
<td>String</td>
<td>RC4加密文件路径</td>
</tr>
<tr>
<td>响应</td>
<td colspan="5">
<pre>
{
    "code": 200,
    "msg": "成功",
    "data": "true" // true表示删除成功，false表示删除失败
}
</pre>
</td>
</tr>
</table>

#### 下载文件接口

<table>
<tr>
<td>地址</td>
<td colspan="5">/common/file/download?filePath=xxx&fileName=xxx</td>
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
<td></td>
<td></td>
<td></td>
<td></td>
<td></td>
</tr>
<tr>
<td rowspan="2">请求参数</td>
<td>filePath</td>
<td></td>
<td>是</td>
<td>String</td>
<td>RC4加密文件路径</td>
</tr>
<tr>
<td>fileName</td>
<td></td>
<td>否</td>
<td>String</td>
<td>下载文件保存名称</td>
</tr>
<tr>
<td>响应</td>
<td colspan="5">
文件流
</td>
</tr>
</table>

## 构件依赖

日志组件、加解密组件、PDF构件、SERVER核心能力构件。
