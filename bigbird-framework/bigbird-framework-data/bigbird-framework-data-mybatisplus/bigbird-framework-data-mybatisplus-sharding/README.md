# MybatisPlus构件分库分表模块

本构件属于MybatisPlus构件的分库分表模块，提供以下功能：

1、引入了shardingsphere，支持分库分表操作。

2、改进flyway，配置项设置type: sharding，支持自助管理shardingsphere数据源对应数据库脚本。

## 配置

### shardingsphere支持分库分表

1、结合druid与p6spy，因此，配置项有些注意事项见配置注释。

2、分库分表规则基于4.1.1版本配置，说明网址：https://shardingsphere.apache.org/document/4.1.1/cn/overview/

```
spring:
  shardingsphere:
    datasource:
      # 配置数据源，给数据源起名ds1,ds2...此处可配置多数据源
      names: ds1,ds2
      # 配置数据源具体内容，包含：连接池，驱动，地址，用户名，密码
      ds1:
        url: jdbc:p6spy:mysql://localhost:3306/mybatis_plus3?createDatabaseIfNotExist=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_bin&zeroDateTimeBehavior=convertToNull&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Shanghai
        username: root
        password: root
        driver-class-name: com.p6spy.engine.spy.P6SpyDriver
        type: com.alibaba.druid.pool.DruidDataSource
        # 配置数据库类型，解决wall过滤器与p6spy冲突
        dbType: mysql
        # 数据库连接池的其它属性，通过反射执行com.alibaba.druid.pool.DruidDataSource中的set方法设置属性
        initial-size: 5
        max-active: 20
        min-idle: 10
        max-wait: 60000
        time-between-eviction-runs-millis: 300000
        min-evictable-idle-time-millis: 600000
        max-evictable-idle-time-millis: 900000
        validationQuery: SELECT 1 FROM DUAL
        testWhileIdle: false
        testOnBorrow: false
        testOnReturn: false
        poolPreparedStatements: true
        max-pool-prepared-statement-per-connection-size: 20
        keepAlive: true
        use-global-data-source-stat: true
        # 必须配置在connection-properties之前
        filters: stat,wall,slf4j
        connection-properties: druid.stat.mergeSql=false;druid.stat.slowSqlMillis=3000;druid.stat.logSlowSql=true;druid.wall.multiStatementAllow=true;druid.wall.throwException=false;druid.log.stmt.executableSql=true;
      ds2:
        url: jdbc:p6spy:mysql://localhost:3306/mybatis_plus4?createDatabaseIfNotExist=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_bin&zeroDateTimeBehavior=convertToNull&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Shanghai
        username: root
        password: root
        driver-class-name: com.p6spy.engine.spy.P6SpyDriver
        type: com.alibaba.druid.pool.DruidDataSource
        # 配置数据库类型，解决wall过滤器与p6spy冲突
        dbType: mysql
        # 数据库连接池的其它属性，通过反射执行com.alibaba.druid.pool.DruidDataSource中的set方法设置属性
        initial-size: 5
        max-active: 20
        min-idle: 10
        max-wait: 60000
        time-between-eviction-runs-millis: 300000
        min-evictable-idle-time-millis: 600000
        max-evictable-idle-time-millis: 900000
        validationQuery: SELECT 1 FROM DUAL
        testWhileIdle: false
        testOnBorrow: false
        testOnReturn: false
        poolPreparedStatements: true
        max-pool-prepared-statement-per-connection-size: 20
        keepAlive: true
        use-global-data-source-stat: true
        # 必须配置在connection-properties之前
        filters: stat,wall,slf4j
        connection-properties: druid.stat.mergeSql=false;druid.stat.slowSqlMillis=3000;druid.stat.logSlowSql=true;druid.wall.multiStatementAllow=true;druid.wall.throwException=false;druid.log.stmt.executableSql=true;
    sharding:
      # 默认数据源，未分片的表默认执行库
      default-data-source-name: ds2
      # 配置表的分布，表的策略
      tables:
        td_order:
          # 配置表的分布，表的策略
          actual-data-nodes: ds2.td_order_$->{0..2}
          # 指定td_order表 主键id 生成策略为 SNOWFLAKE
          key-generator:
            column: id
            type: SNOWFLAKE
          # 指定分片策略 约定id值%3后取模
          table-strategy:
            standard:
              sharding-column: id
              precise-algorithm-class-name: wang.bigbird.domain.framework.data.mybatisplus.sharding.support.algorithm.MyTablePreciseShardingAlgorithm
    props:
      sql:
        # 展示修改以后的sql语句
        show: false
```

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
  shardingsphere:
    datasource:
      # 配置数据源，给数据源起名ds1,ds2...此处可配置多数据源
      names: ds1,ds2
      # 配置数据源具体内容，包含：连接池，驱动，地址，用户名，密码
      ds1:
        url: jdbc:p6spy:mysql://localhost:3306/mybatis_plus3?createDatabaseIfNotExist=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_bin&zeroDateTimeBehavior=convertToNull&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Shanghai
        username: root
        password: root
        driver-class-name: com.p6spy.engine.spy.P6SpyDriver
        type: com.alibaba.druid.pool.DruidDataSource
        # 配置数据库类型，解决wall过滤器与p6spy冲突
        dbType: mysql
        # 数据库连接池的其它属性，通过反射执行com.alibaba.druid.pool.DruidDataSource中的set方法设置属性
        initial-size: 5
        max-active: 20
        min-idle: 10
        max-wait: 60000
        time-between-eviction-runs-millis: 300000
        min-evictable-idle-time-millis: 600000
        max-evictable-idle-time-millis: 900000
        validationQuery: SELECT 1 FROM DUAL
        testWhileIdle: false
        testOnBorrow: false
        testOnReturn: false
        poolPreparedStatements: true
        max-pool-prepared-statement-per-connection-size: 20
        keepAlive: true
        use-global-data-source-stat: true
        # 必须配置在connection-properties之前
        filters: stat,wall,slf4j
        connection-properties: druid.stat.mergeSql=false;druid.stat.slowSqlMillis=3000;druid.stat.logSlowSql=true;druid.wall.multiStatementAllow=true;druid.wall.throwException=false;druid.log.stmt.executableSql=true;
      ds2:
        url: jdbc:p6spy:mysql://localhost:3306/mybatis_plus4?createDatabaseIfNotExist=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_bin&zeroDateTimeBehavior=convertToNull&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Shanghai
        username: root
        password: root
        driver-class-name: com.p6spy.engine.spy.P6SpyDriver
        type: com.alibaba.druid.pool.DruidDataSource
        # 配置数据库类型，解决wall过滤器与p6spy冲突
        dbType: mysql
        # 数据库连接池的其它属性，通过反射执行com.alibaba.druid.pool.DruidDataSource中的set方法设置属性
        initial-size: 5
        max-active: 20
        min-idle: 10
        max-wait: 60000
        time-between-eviction-runs-millis: 300000
        min-evictable-idle-time-millis: 600000
        max-evictable-idle-time-millis: 900000
        validationQuery: SELECT 1 FROM DUAL
        testWhileIdle: false
        testOnBorrow: false
        testOnReturn: false
        poolPreparedStatements: true
        max-pool-prepared-statement-per-connection-size: 20
        keepAlive: true
        use-global-data-source-stat: true
        # 必须配置在connection-properties之前
        filters: stat,wall,slf4j
        connection-properties: druid.stat.mergeSql=false;druid.stat.slowSqlMillis=3000;druid.stat.logSlowSql=true;druid.wall.multiStatementAllow=true;druid.wall.throwException=false;druid.log.stmt.executableSql=true;
    sharding:
      # 默认数据源，未分片的表默认执行库
      default-data-source-name: ds2
      # 配置表的分布，表的策略
      tables:
        td_order:
          # 配置表的分布，表的策略
          actual-data-nodes: ds2.td_order_$->{0..2}
          # 指定td_order表 主键id 生成策略为 SNOWFLAKE
          key-generator:
            column: id
            type: SNOWFLAKE
          # 指定分片策略 约定id值%3后取模
          table-strategy:
            standard:
              sharding-column: id
              precise-algorithm-class-name: wang.bigbird.domain.framework.data.mybatisplus.sharding.support.algorithm.MyTablePreciseShardingAlgorithm
    props:
      sql:
        # 展示修改以后的sql语句
        show: false
  flyway:
    # 取值为dynamic或者sharding
    type: sharding
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
  # 以下配置为解决4.1.1版本shardingsphere在执行时间字段转换上的BUG专门设置
  type-handlers-package: wang.bigbird.domain.framework.data.mybatisplus.dynamic.support.handler
  configuration:
    map-underscore-to-camel-case: true
    # 关闭默认的日期类型处理器（进一步确保自定义处理器生效）
    default-local-date-time-type-handler: wang.bigbird.domain.framework.data.mybatisplus.dynamic.support.handler.LocalDateTimeTypeHandler
  
```
