# SERVER通用能力构件网络模块

本构件属于统一SERVER通用能力的网络请求模块，提供以下功能：

1、提供涉及调用外部Rest接口的签名处理器，具体构造方式如下：

```
@RetrofitClient(baseUrl = "${baseUrl}")
@Sign(appKey = "${app.key}", appSecret = "${app.secret}")
public interface XxxHttpClient {
}
```

2、提供支持https请求的Retrofit客户端构造器，具体构造方式如下：

```
@Bean
public XxxHttpClient xxxHttpClient(OkHttpClient okHttpClient) {
    Retrofit retrofit = new Retrofit.Builder().baseUrl(authBaseUrl)
            .addConverterFactory(JacksonConverterFactory.create())
            .client(okHttpClient)
            .build();
    return retrofit.create(XxxHttpClient.class);
}
```

3、提供支持SSE请求的Retrofit客户端构造器，具体构造方式如下：

```
@Bean
public XxxHttpClient xxxHttpClient(OkHttpClient sseHttpClient) {
    Retrofit retrofit = new Retrofit.Builder().baseUrl(authBaseUrl)
            .addConverterFactory(JacksonConverterFactory.create())
            .client(sseHttpClient)
            .build();
    return retrofit.create(XxxHttpClient.class);
}
```

4、提供支持流式响应的回调处理器，具体使用方式如下：

```
// 关键！不加@Streaming，OkHttp会全缓存完再返回
// 流式返回接口 → 必须用 ResponseBody 接收
@Streaming 
@POST("xxx")
Call<ResponseBody> callMethod(xx);

@Autowired
private AsyncTaskExecutor asyncTaskExecutor;
asyncTaskExecutor.execute(() -> {
    Call<ResponseBody> call = retrofitHttpClient.callMethod(xx);
    StreamResponseHandler streamResponseHandler = new StreamResponseHandler(call, new IStreamCallbacker() {

        @Override
        public void onStart() throws IOException {
           // 响应开始的业务处理，可以在这里利用SseEmitter emitter输出响应开始提示
           // emitter.send();
        }

        @Override
        public void onProcess(String data) throws IOException {
           // 中间获得每行数据的业务处理，可以在这里利用SseEmitter emitter输出实时数据
           // emitter.send(data);
        }

        @Override
        public void onSuccess(String fullData) {
           // 成功获得完整数据的业务处理，可以在这里设置SseEmitter emitter输出完毕
           // emitter.complete();
        }

        @Override
        public void onFailed(Throwable throwable) {
           // 流式响应失败的业务处理，可以在这里利用SseEmitter emitter输出错误
           // emitter.completeWithError(throwable);
        }
              
    });
    streamResponseHandler.handleResponse();
});
```

## 配置

本构件有以下重要配置，描述如下：

### 网络请求公共配置

```
bigbird:
  server:
    common:
      retrofit:
        # 连接超时时间，毫秒为单位，推荐 30~60 秒
        connect-timeout-ms: 30000
        # 读取超时时间，毫秒为单位，推荐 30~60 秒
        read-timeout-ms: 30000
        # 写入超时时间，毫秒为单位，推荐 30~60 秒
        write-timeout-ms: 30000
        # 支持 none、basic、headers、body
        level: body  
        # 序列化长度，设置合理的长度可以防止打印日志过长使JVM内存溢出
        serialize-length: 2048 

retrofit:
  enable-response-call-adapter: true
  # 全局转换器
  global-converter-factories:
    - retrofit2.converter.jackson.JacksonConverterFactory
  # 启用日志打印
  enable-log: true
  # 是否启用熔断降级
  enable-degrade: true
  # 全局连接超时时间，还有局部的，在@RetrofitClient注解上可以设置超时时间，针对当前接口生效，优先级更高。具体字段有connectTimeoutMs、readTimeoutMs、writeTimeoutMs、callTimeoutMs等
  global-connect-timeout-ms: 30000
  # 全局读取超时时间
  global-read-timeout-ms: 30000
  # 全局写入超时时间
  global-write-timeout-ms: 30000
  # 全局完整调用超时时间
  global-call-timeout-ms: 0
  # 连接池配置
  pool:
    test1:
      max-idle-connections: 3
      keep-alive-second: 100
    test2:
      max-idle-connections: 5
      keep-alive-second: 50
```

### 签名注解配置

样例：@Sign(appKey = "${app.key}", appSecret = "${app.secret}")

```
app:
  key: xxx
  secret: xxx
```

## 构件依赖

日志组件、SERVER核心能力构件。
