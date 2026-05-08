# OSS构件

本构件用于管理OSS相关的数据持久化操作，提供支持MinIO，阿里云，腾讯云，华为云，天翼云五种云对象存储业务以及FTP远程文件存储业务和分布式文件存储业务。

```
// 云对象存储服务
@Autowired
private OssPersistenceService ossPersistenceService;
```

## 基于MinIO的云对象存储

```
bigbird:
  data:
    oss:
      key: # 加解密密钥
      type: minio
      minio:
        url: 
        accessKey: 
        secretKey: 
        secure: #true采用https，false采用http，默认值是false
```

## 基于阿里云的云对象存储

```
bigbird:
  data:
    oss:
      key: # 加解密密钥
      type: aliyun
      aliyun:
        endpoint: 
        accessKeyId: 
        accessKeySecret: 
```

## 基于腾讯云的云对象存储

```
bigbird:
  data:
    oss:
      key: # 加解密密钥
      type: qcloud
      qcloud:
        appId: 
        secretId: 
        secretKey: 
        referer: 
```

## 基于华为云的云对象存储

```
bigbird:
  data:
    oss:
      key: # 加解密密钥
      type: huawei
      huawei:
        endpoint: 
        ak: 
        sk: 
        protocol: # 链接协议，http或者https
```

## 基于天翼云的云对象存储

```
bigbird:
  data:
    oss:
      key: # 加解密密钥
      type: ct
      ct:
        endpoint: 
        accessId: 
        accessKey: 
        protocol: # 链接协议，http或者https
        connectionTimeout: # 连接的超时时间，单位毫秒
        socketTimeout: # socket超时时间，单位毫秒
```

## 基于FTP的远程文件存储

```
bigbird:
  data:
    oss:
      key: # 加解密密钥
      type: ftp
      ftp:
        host: # FTP服务器IP
        port: # FTP服务端口
        username: # 授权用户名
        password: # 授权密码
```

## 基于FastDFS的分布式文件存储

```
bigbird:
  data:
    oss:
      type: dfs

fdfs:
  # 1. 连接池配置（核心，影响性能和稳定性）
  pool:
    max-total: 100                  # 连接池最大连接数（默认8，并发高时调大），限制客户端总并发连接数，避免连接耗尽
    max-total-per-key: 8            # 控制单个 Tracker/Storage 节点的最大连接数，均衡集群压力
    max-idle-per-key: 8             # 限制每个 Tracker/Storage 节点的空闲连接上限，避免单个节点占用过多空闲连接导致资源浪费
    min-idle-per-key: 2             # 控制单个 Tracker/Storage 节点保持最小空闲连接，避免频繁新建/关闭连接，提升请求响应速度
    max-wait-millis: 3000           # 当连接池无空闲连接时，最大等待时间（毫秒，默认-1：无限等待）
    test-on-borrow: true            # 借出连接时是否校验可用性（默认false，建议true，避免使用无效连接）
    test-on-return: false           # 归还连接时是否校验可用性（默认false）
    test-while-idle: true           # 空闲时定时清理无效连接（true则定期校验空闲连接，避免连接泄露）
    time-between-eviction-runs-millis: 60000  # 空闲连接检测周期（毫秒，默认60000）
    min-evictable-idle-time-millis: 300000    # 连接最小空闲时间（毫秒，默认300000，超过则被清理）

  # 2. 缩略图配置
  thumb-image:             
    width: 150                      # 缩略图宽度
    height: 150                     # 缩略图高度

  # 3. Tracker 服务器配置（FastDFS 入口，必填）
  tracker-list:                     # Tracker 集群地址，多个用逗号分隔（格式：ip:port）
    - 192.168.1.100:22122           # 示例：Tracker 1
    - 192.168.1.101:22122           # 示例：Tracker 2（集群可选）
  web-server-url: https://file.example.com  # 文件访问的全局统一前缀URL（生产环境用自定义域名）
  connect-timeout: 5000             # 与 Tracker 建立连接的超时时间（毫秒，默认5000）
  so-timeout: 30000                 # 与 Tracker/Storage 通信的读取超时时间（毫秒，默认30000）
  charset: UTF-8                    # 字符编码（默认UTF-8，无需修改）
```

## 注意事项

本构件需要将天翼云依赖jar包（lib/oos-java-sdk-6.5.3.jar）手工安装到maven本地仓库或者私有仓库中。

```
# 进入lib目录，执行如下命令：

mvn install:install-file \
-Dfile=./zos-java-sdk-s3.jar \
-DgroupId=cn.ctyun \
-DartifactId=zos-java-sdk-s3 \
-Dversion=1.0.0 \
-Dpackaging=jar \
-DgeneratePom=true

mvn install:install-file \
-Dfile=./zos-java-sdk-sts.jar \
-DgroupId=cn.ctyun \
-DartifactId=zos-java-sdk-sts \
-Dversion=1.0.0 \
-Dpackaging=jar \
-DgeneratePom=true
```
