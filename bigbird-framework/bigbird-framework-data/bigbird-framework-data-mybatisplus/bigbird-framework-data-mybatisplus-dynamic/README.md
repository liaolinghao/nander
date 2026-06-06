# MybatisPlus构件多数据源模块

本构件属于MybatisPlus构件的多数据源模块，提供以下功能：

1、管理MybatisPlus相关的数据持久化操作，对于集成MybatisPlus时统一配置了一些更实用的方法。

比如：insertBatchSomeColumn（高效的批量添加）、insertIgnoreBatchSomeColumn（高效的批量添加且自动忽略重复数据）、alwaysUpdateSomeColumnById和deleteByIdWithFill。

使用时，具体业务可以通过继承以下实体类直接获得有关数据常规操作（增删改查）的支持，具体如下：

Entity层：物理删除继承BaseEntity，逻辑删除继承BaseLogicEntity，对应数据表需要有id（主键），createTime，updateTime，deleted四个字段。

DAO层：继承BaseMapper。

SERVICE接口层：继承IService。

SERVICE接口实现层：继承AbstractServiceImpl。

2、引入了动态多数据源dynamic，支持服务操作单库或者多库，在涉及跨库的事务时，需要给相关方法配置注解：@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)，具体说明如下：

Spring在单一事务中从固定数据库连接池获取唯一链接，因此，不开启新事务无法实现切换数据源。

REQUIRES_NEW：新建事务，运行机制为：如果当前存在事务，则把当前事务挂起，这个方法会独立提交事务，不受调用者的事务影响，父级异常，它也是正常提交，因此，有该注解的方法放在父级业务逻辑执行完毕后再执行更合适。此时，它如果执行异常可以使父级事务回滚，使整体业务逻辑保持一致。

更为复杂的事务场景，如：父级业务涉及数据源超过2个，则应该考虑分布式事务解决方案（例如：Seata）。

3、引入了druid监控，支持分析sql语句的性能与增强sql操作安全。

4、引入了p6spy，美化SQL日志，显示真实的sql操作语句与耗费时间。

5、引入了flyway，自助管理数据库脚本（注意：脚本中尽量避免包含数据库名称，如果必须包含数据库名称，用内置变量${flyway:database}代替数据库名称。），自动完成：建库建表，初始化数据，无需手工介入。

6、提供数据字段自动加解密机制，只要为entity实体类中字段配置@SecurityField注解即可实现，加密数据值采用ENC(密文)方式展示。

## 配置

本构件有五类重要配置，分别描述如下：

### 数据字段加解密密钥

1、对于加密字段的查询，需要在代码中将条件值进行加密，并且加密字段不再支持条件查询。

```
bigbird:
  data:
    mybatisplus:
      key: bigbird
```

### dynamic支持多数据源，实现数据源自动切换操作

1、在controller、service、dao类或方法上添加注解@DS("slave")即可基于对应数据源执行数据操作。

2、可采用工具类对明文执行加密后，采用ENC(密文)方式配置url，username和password。

```
spring:
  datasource:
    dynamic:
      # 全局druid参数，单独数据源配置为空时取全局配置
      druid:
        # 配置初始化大小、最小、最大
        initial-size: 5
        max-active: 20
        min-idle: 10
        # 配置获取连接等待超时的时间（单位：毫秒）
        max-wait: 60000
        # 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
        time-between-eviction-runs-millis: 300000
        # 配置一个连接在池中最小生存的时间，单位是毫秒
        min-evictable-idle-time-millis: 600000
        max-evictable-idle-time-millis: 900000
        # 用来测试连接是否可用的SQL语句，默认值每种数据库都不相同，这是mysql
        validationQuery: SELECT 1 FROM DUAL
        # 应用向连接池申请连接，并且testOnBorrow为false时，连接池将会判断连接是否处于空闲状态，如果是，则验证这条连接是否可用
        testWhileIdle: false
        # 如果为true，默认是false，应用向连接池申请连接时，连接池会判断这条连接是否是可用的
        testOnBorrow: false
        # 如果为true（默认false），当应用使用完连接，连接池回收连接的时候会判断该连接是否还可用
        testOnReturn: false
        # 是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大，比如说oracle，在mysql下建议关闭。
        poolPreparedStatements: true
        # 要启用PSCache，必须配置大于0，当大于0时， poolPreparedStatements自动触发修改为true，
        # 在Druid中，不会存在Oracle下PSCache占用内存过多的问题，
        # 可以把这个数值配置大一些，比如说100
        max-pool-prepared-statement-per-connection-size: 20
        # 连接池中的minIdle数量以内的连接，空闲时间超过minEvictableIdleTimeMillis，则会执行keepAlive操作
        keepAlive: true
        # 缺省多个DruidDataSource的监控数据是各自独立的，在druid-0.2.17版本之后，支持配置公用监控数据
        # 合并多个DruidDataSource的监控数据
        use-global-data-source-stat: true
        # 配置监控统计拦截的filters
        # stat：监控统计（必须配置，否则监控不到SQL）、slf4j：日志记录、wall：防御sql注入
        filters: stat,wall,slf4j
        # 开启druid datasource的状态监控
        stat:
          # 开启慢sql监控，超过3s就认为是慢sql，记录到日志中
          slow-sql-millis: 3000
          log-slow-sql: true
          # 当程序中存在没有参数化的sql执行时，sql统计的效果会不好。比如：
          # select * from t where id = 1
          # select * from t where id = 2
          # select * from t where id = 3
          # 在统计中，显示为3条sql，这不是希望要的效果。
          # StatFilter提供合并的功能，能够将这3个SQL合并为如下的SQL：
          # select * from t where id = ?
          # 关闭合并可以避免对某些sql解析错误从而提示异常
          merge-sql: true
        wall:
          # 是否允许一次执行多条语句
          multi-statement-allow: true
          # 是否进行严格的语法检测，Druid SQL Parser在某些场景不能覆盖所有的SQL语法，出现解析SQL出错，可以临时把这个选项设置为false，同时把SQL反馈给Druid的开发者。
          strictSyntaxCheck: false          
        # 日志监控，使用slf4j 进行日志输出
        slf4j:
          enabled: true
          statement-executable-sql-log-enable: true
      # dynamic主从设置
      # 设置默认的数据源或者数据源组，默认值即为master
      primary: master
      # 设置严格模式，默认false不启动。启动后在未匹配到指定数据源时候会抛出异常，不启动会使用默认数据源。
      strict: false
      # 是否使用p6spy输出，默认不输出，设置为true会打印重复日志
      p6spy: false
      datasource:
        master:
          url: jdbc:p6spy:mysql://localhost:3306/mybatis_plus?createDatabaseIfNotExist=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_bin&zeroDateTimeBehavior=convertToNull&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Shanghai
          username: root
          password: root
          driver-class-name: com.p6spy.engine.spy.P6SpyDriver
          type: com.alibaba.druid.pool.DruidDataSource
        slave:
          url: jdbc:p6spy:mysql://localhost:3306/mybatis_plus2?createDatabaseIfNotExist=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_bin&zeroDateTimeBehavior=convertToNull&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Shanghai
          username: root
          password: root
          driver-class-name: com.p6spy.engine.spy.P6SpyDriver
          type: com.alibaba.druid.pool.DruidDataSource
```

### druid监控

基于druid实现对数据持久化操作的监控，监控网址：/druid/login.html

```
spring:
  autoconfigure:
    exclude: com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure
  datasource:
    druid:
      # 统计web应用请求中所有的数据库信息，比如：发出的sql语句，sql执行的时间、请求次数、请求的url地址、以及seesion监控、数据库表的访问次数等等
      web-stat-filter:
        # 启动 StatFilter
        enabled: true
        # 过滤所有url
        url-pattern: /*
        # 排除一些不必要的url
        exclusions: "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*"
        # 开启session统计功能
        session-stat-enable: true
        # session的最大个数，默认100
        session-stat-max-count: 1000
      # 配置StatViewServlet（监控页面），用于展示Druid的统计信息
      stat-view-servlet:
        # 启用StatViewServlet
        enabled: true
        # 访问内置监控页面的路径，内置监控页面的首页是/druid/index.html
        url-pattern: /druid/*
        # 不允许清空统计数据，重新计算
        reset-enable: false
        # 配置监控页面访问账号和密码
        login-username: root
        login-password: passw0rd
        # 允许访问的地址，如果allow没有配置或者为空，则允许所有访问
        allow: 127.0.0.1
        # 拒绝访问的地址，deny优先于allow，如果在deny列表中，就算在allow列表中，也会被拒绝
        deny:
```

### p6spy美化SQL日志

首先，排除以下配置，以去除原始的SQL语句打印功能。

```
# 打印sql语句
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

其次，增加以下配置，以采用自定义格式打印执行的SQL语句与性能。

```
decorator:
  datasource:
    p6spy:
      logging: custom
      custom-appender-class: wang.bigbird.domain.framework.data.mybatisplus.dynamic.support.logger.MyFormattedLogger
```

显示如下：

```
执行时刻：2023-07-18 17:29:06
SQL耗时：1毫秒
连接信息：jdbc:p6spy:mysql://localhost:3306/mybatis_plus?createDatabaseIfNotExist=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_bin&zeroDateTimeBehavior=convertToNull&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Shanghai
执行SQL：SELECT id,name,age,email,create_time,update_time FROM user WHERE id=1546747253062778882
```

### flyway管理数据库脚本

首先，在项目类路径增加数据库脚本存放目录：db/数据源名称/migration

其次，增加以下配置，以使flyway按指定格式解析数据库脚本并执行。

```
spring:
  flyway:
    # 取值为dynamic或者sharding
    type: dynamic
    # 是否启用flyway，由于支持多数据源，这里设置为false
    enabled: false
    # 编码格式，默认UTF-8
    encoding: UTF-8
    # 迁移sql脚本文件名称的前缀，默认V
    sql-migration-prefix: V
    # 迁移sql脚本文件名称的分隔符，默认2个下划线__
    sql-migration-separator: __
    # 迁移sql脚本文件名称的后缀
    sql-migration-suffixes: .sql
    # 迁移时是否进行校验，默认true
    validate-on-migrate: true
    # 当迁移发现数据库非空且存在没有元数据的表时，自动执行基准迁移，新建schema_version表
    baseline-on-migrate: true
    # 禁止flyway执行清理
    clean-disabled: true
    # 用于记录所有的版本变化记录
    table: flyway_schema_history
```

再次，为支持以指定编码utf8mb4创建数据库，以下三个细节需要注意：

1、数据库连接语句需要设置：createDatabaseIfNotExist=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_bin&zeroDateTimeBehavior=convertToNull&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Shanghai

2、第一个执行的数据库脚本：ALTER DATABASE ${flyway:database} CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

3、上述修改数据库编码语句无法被druid正确解析，为此，druid如果开启了wall，那么必须设置：strictSyntaxCheck: false，以防止修改数据库编码语句执行失败。

完整配置样例描述如下：

```
spring:
  # druidDataSourceCreator定义存在冲突，需要开启以下配置
  main:
    allow-bean-definition-overriding: true
  autoconfigure:
    exclude: com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure
  datasource:
    druid:
      # 统计web应用请求中所有的数据库信息，比如：发出的sql语句，sql执行的时间、请求次数、请求的url地址、以及seesion监控、数据库表的访问次数等等
      web-stat-filter:
        # 启动 StatFilter
        enabled: true
        # 过滤所有url
        url-pattern: /*
        # 排除一些不必要的url
        exclusions: "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*"
        # 开启session统计功能
        session-stat-enable: true
        # session的最大个数，默认100
        session-stat-max-count: 1000
      # 配置StatViewServlet（监控页面），用于展示Druid的统计信息
      stat-view-servlet:
        # 启用StatViewServlet
        enabled: true
        # 访问内置监控页面的路径，内置监控页面的首页是/druid/index.html
        url-pattern: /druid/*
        # 不允许清空统计数据，重新计算
        reset-enable: false
        # 配置监控页面访问账号和密码
        login-username: root
        login-password: passw0rd
        # 允许访问的地址，如果allow没有配置或者为空，则允许所有访问
        allow: 127.0.0.1
        # 拒绝访问的地址，deny优先于allow，如果在deny列表中，就算在allow列表中，也会被拒绝
        deny:
    dynamic:
      # 全局druid参数，单独数据源配置为空时取全局配置，具体见
      # com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidConfig
      druid:
        # 配置初始化大小、最小、最大
        initial-size: 5
        max-active: 20
        min-idle: 10
        # 配置获取连接等待超时的时间（单位：毫秒）
        max-wait: 60000
        # 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
        time-between-eviction-runs-millis: 300000
        # 配置一个连接在池中最小生存的时间，单位是毫秒
        min-evictable-idle-time-millis: 600000
        max-evictable-idle-time-millis: 900000
        # 用来测试连接是否可用的SQL语句，默认值每种数据库都不相同，这是mysql
        validationQuery: SELECT 1 FROM DUAL
        # 应用向连接池申请连接，并且testOnBorrow为false时，连接池将会判断连接是否处于空闲状态，如果是，则验证这条连接是否可用
        testWhileIdle: false
        # 如果为true，默认是false，应用向连接池申请连接时，连接池会判断这条连接是否是可用的
        testOnBorrow: false
        # 如果为true（默认false），当应用使用完连接，连接池回收连接的时候会判断该连接是否还可用
        testOnReturn: false
        # 是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大，比如说oracle，在mysql下建议关闭。
        poolPreparedStatements: true
        # 要启用PSCache，必须配置大于0，当大于0时， poolPreparedStatements自动触发修改为true，
        # 在Druid中，不会存在Oracle下PSCache占用内存过多的问题，
        # 可以把这个数值配置大一些，比如说100
        max-pool-prepared-statement-per-connection-size: 20
        # 连接池中的minIdle数量以内的连接，空闲时间超过minEvictableIdleTimeMillis，则会执行keepAlive操作
        keepAlive: true
        # 缺省多个DruidDataSource的监控数据是各自独立的，在druid-0.2.17版本之后，支持配置公用监控数据
        # 合并多个DruidDataSource的监控数据
        use-global-data-source-stat: true
        # 配置监控统计拦截的filters
        # stat：监控统计（必须配置，否则监控不到SQL）、slf4j：日志记录、wall：防御sql注入
        filters: stat,wall,slf4j
        # 开启druid datasource的状态监控
        stat:
          # 开启慢sql监控，超过3s就认为是慢sql，记录到日志中
          slow-sql-millis: 3000
          log-slow-sql: true
          # 当程序中存在没有参数化的sql执行时，sql统计的效果会不好。比如：
          # select * from t where id = 1
          # select * from t where id = 2
          # select * from t where id = 3
          # 在统计中，显示为3条sql，这不是希望要的效果。
          # StatFilter提供合并的功能，能够将这3个SQL合并为如下的SQL：
          # select * from t where id = ?
          # 关闭合并可以避免对某些sql解析错误从而提示异常
          merge-sql: false
        wall:
          # 是否允许一次执行多条语句
          multi-statement-allow: true
          # 是否进行严格的语法检测，Druid SQL Parser在某些场景不能覆盖所有的SQL语法，出现解析SQL出错，可以临时把这个选项设置为false，同时把SQL反馈给Druid的开发者。
          strictSyntaxCheck: false
        # 日志监控，使用slf4j 进行日志输出
        slf4j:
          enabled: true
          statement-executable-sql-log-enable: true
      # dynamic主从设置
      # 设置默认的数据源或者数据源组，默认值即为master
      primary: master
      # 设置严格模式，默认false不启动。启动后在未匹配到指定数据源时候会抛出异常，不启动会使用默认数据源。
      strict: false
      datasource:
        master:
          url: jdbc:p6spy:mysql://localhost:3306/mybatis_plus1?createDatabaseIfNotExist=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_bin&zeroDateTimeBehavior=convertToNull&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Shanghai
          username: root
          password: ENC(VZamSTMi224AH6RUtJGXNldiDp/XEL2ozRhBUu/o9ChodT4JEb9kE/j0EFhXKbjsfvLVacUW0AUzetA6OrNJug==)
          driver-class-name: com.p6spy.engine.spy.P6SpyDriver
          type: com.alibaba.druid.pool.DruidDataSource
        slave:
          url: jdbc:p6spy:mysql://localhost:3306/mybatis_plus2?createDatabaseIfNotExist=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_bin&zeroDateTimeBehavior=convertToNull&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Shanghai
          username: root
          password: ENC(VZamSTMi224AH6RUtJGXNldiDp/XEL2ozRhBUu/o9ChodT4JEb9kE/j0EFhXKbjsfvLVacUW0AUzetA6OrNJug==)
          driver-class-name: com.p6spy.engine.spy.P6SpyDriver
          type: com.alibaba.druid.pool.DruidDataSource
  flyway:
    # 取值为dynamic或者sharding
    type: dynamic
    # 是否启用flyway，由于支持多数据源，这里设置为false
    enabled: false
    # 编码格式，默认UTF-8
    encoding: UTF-8
    # 迁移sql脚本文件名称的前缀，默认V
    sql-migration-prefix: V
    # 迁移sql脚本文件名称的分隔符，默认2个下划线__
    sql-migration-separator: __
    # 迁移sql脚本文件名称的后缀
    sql-migration-suffixes: .sql
    # 迁移时是否进行校验，默认true
    validate-on-migrate: true
    # 当迁移发现数据库非空且存在没有元数据的表时，自动执行基准迁移，新建schema_version表
    baseline-on-migrate: true
    # 禁止flyway执行清理
    clean-disabled: true
    # 用于记录所有的版本变化记录
    table: flyway_schema_history

decorator:
  datasource:
    p6spy:
      logging: custom
      custom-appender-class: wang.bigbird.domain.framework.data.mybatisplus.dynamic.support.logger.MyFormattedLogger

mybatis-plus:
  mapper-locations: classpath*:mappers/*.xml
  
bigbird:
  data:
    mybatisplus:
      key: bigbird
```
