/*
 * Copyright (c) 2026 廖凌浩 / 鸟域
 *
 * Licensed under the Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package wang.bigbird.domain.framework.server.common.quartz.config.configuration;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.Data;
import org.quartz.utils.ConnectionProvider;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 自定义Quartz使用Druid数据源
 * 字段与quartz.properties中数据源配置的key保持一致，
 * 同时提供set方法，Quartz框架自动注入值。
 *
 * @author Bigbird
 */
@Data
public class DruidQuartzProviderConfiguration implements ConnectionProvider {

    /**
     * JDBC驱动
     */
    public String driver;
    /**
     * JDBC连接串
     */
    public String URL;
    /**
     * 数据库用户名
     */
    public String user;
    /**
     * 数据库用户密码
     */
    public String password;
    /**
     * 数据库最大连接数
     */
    public int maxConnections = 5;
    /**
     * 最小空闲连接数
     */
    public int minIdle = 1;
    /**
     * 获取连接等待超时的时间（单位：毫秒），
     * 0表示这种等待没有超时限制，应用程序会一直阻塞，直到有连接被释放并可用
     */
    public long maxWait = 0;
    /**
     * 配置每个数据库连接对应的PreparedStatement池的最大大小的方法。
     * 它用于控制单个数据库连接上缓存的PreparedStatement对象数量，以优化SQL执行性能
     */
    public int maxPoolPreparedStatementPerConnectionSize = 10;
    /**
     * 用来测试连接是否可用的SQL语句
     */
    public String validationQuery;
    /**
     * 连接验证查询超时时间，秒为单位
     */
    private int idleConnectionValidationSeconds = 1;

    /**
     * Druid连接池
     */
    private DruidDataSource datasource;

    @Override
    public Connection getConnection() throws SQLException {
        return datasource.getConnection();
    }

    @Override
    public void shutdown() {
        datasource.close();
    }

    @Override
    public void initialize() throws SQLException {
        if (StringUtils.isBlank(URL)) {
            throw new SQLException("DBPool could not be created: DB URL cannot be blank");
        }
        if (StringUtils.isBlank(driver)) {
            throw new SQLException("DBPool could not be created: DB driver class name cannot be blank!");
        }
        if (maxConnections < 1) {
            throw new SQLException("DBPool could not be created: Max connections must be greater than zero!");
        }
        datasource = new DruidDataSource();
        datasource.setDriverClassName(driver);
        datasource.setUrl(URL);
        datasource.setUsername(user);
        datasource.setPassword(password);
        datasource.setMaxActive(maxConnections);
        datasource.setMinIdle(minIdle);
        datasource.setMaxWait(maxWait);
        datasource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
        if (StringUtils.isNotBlank(validationQuery)) {
            datasource.setValidationQuery(validationQuery);
            datasource.setTestOnReturn(true);
            datasource.setTestOnBorrow(true);
            datasource.setValidationQueryTimeout(idleConnectionValidationSeconds);
        }
    }
}
