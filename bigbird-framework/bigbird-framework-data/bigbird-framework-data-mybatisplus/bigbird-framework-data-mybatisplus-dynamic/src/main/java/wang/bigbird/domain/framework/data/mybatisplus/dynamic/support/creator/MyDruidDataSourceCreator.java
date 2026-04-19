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
package wang.bigbird.domain.framework.data.mybatisplus.dynamic.base.tools.support;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.baomidou.dynamic.datasource.creator.DruidDataSourceCreator;
import com.baomidou.dynamic.datasource.exception.ErrorCreateDataSourceException;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidConfig;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidSlf4jConfig;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidWallConfigUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 改良的Druid数据源创建器
 *
 * @author Bigbird
 */
public class MyDruidDataSourceCreator extends DruidDataSourceCreator {

    private static final String FILTER_TYPE_STAT = "stat";
    private static final String FILTER_TYPE_WALL = "wall";
    private static final String FILTER_TYPE_SLF4J = "slf4j";

    private static final String JDBC_P6SPY_PREFIX = "jdbc:p6spy:";
    private static final String JDBC_PREFIX = "jdbc:";

    private DruidConfig druidConfig;

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    public MyDruidDataSourceCreator(DruidConfig druidConfig) {
        super(druidConfig);
        this.druidConfig = druidConfig;
    }

    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
        DruidDataSource dataSource = new DruidDataSource();
        configureBasicProperties(dataSource, dataSourceProperty);
        configureDruid(dataSource, dataSourceProperty);
        try {
            dataSource.init();
        } catch (SQLException e) {
            throw new ErrorCreateDataSourceException("druid create error", e);
        }
        return dataSource;
    }

    private void configureBasicProperties(DruidDataSource dataSource, DataSourceProperty property) {
        dataSource.setUsername(property.getUsername());
        dataSource.setPassword(property.getPassword());
        dataSource.setUrl(property.getUrl());
        dataSource.setDbType(parseDbType(property.getUrl()));
        dataSource.setName(property.getPoolName());
        String driverClassName = property.getDriverClassName();
        if (StringUtils.isNotBlank(driverClassName)) {
            dataSource.setDriverClassName(driverClassName);
        }
    }

    private void configureDruid(DruidDataSource dataSource, DataSourceProperty property) {
        DruidConfig config = property.getDruid();
        Properties properties = config.toProperties(druidConfig);
        configureFilters(dataSource, properties, property);
        dataSource.configFromPropety(properties);
        configureConnectProperties(dataSource, config);
    }

    private void configureConnectProperties(DruidDataSource dataSource, DruidConfig config) {
        //连接参数单独设置
        dataSource.setConnectProperties(config.getConnectionProperties());
        //设置druid内置properties不支持的的参数
        Boolean testOnReturn = config.getTestOnReturn() == null ? druidConfig.getTestOnReturn() : config.getTestOnReturn();
        if (testOnReturn != null && testOnReturn.equals(true)) {
            dataSource.setTestOnReturn(true);
        }
        Integer validationQueryTimeout =
                config.getValidationQueryTimeout() == null ? druidConfig.getValidationQueryTimeout() : config.getValidationQueryTimeout();
        if (validationQueryTimeout != null && !validationQueryTimeout.equals(-1)) {
            dataSource.setValidationQueryTimeout(validationQueryTimeout);
        }
        Boolean sharePreparedStatements =
                config.getSharePreparedStatements() == null ? druidConfig.getSharePreparedStatements() : config.getSharePreparedStatements();
        if (sharePreparedStatements != null && sharePreparedStatements.equals(true)) {
            dataSource.setSharePreparedStatements(true);
        }
        Integer connectionErrorRetryAttempts =
                config.getConnectionErrorRetryAttempts() == null ? druidConfig.getConnectionErrorRetryAttempts()
                        : config.getConnectionErrorRetryAttempts();
        if (connectionErrorRetryAttempts != null && !connectionErrorRetryAttempts.equals(1)) {
            dataSource.setConnectionErrorRetryAttempts(connectionErrorRetryAttempts);
        }
        Boolean breakAfterAcquireFailure =
                config.getBreakAfterAcquireFailure() == null ? druidConfig.getBreakAfterAcquireFailure() : config.getBreakAfterAcquireFailure();
        if (breakAfterAcquireFailure != null && breakAfterAcquireFailure.equals(true)) {
            dataSource.setBreakAfterAcquireFailure(true);
        }
        Integer timeout = config.getRemoveAbandonedTimeoutMillis() == null ? druidConfig.getRemoveAbandonedTimeoutMillis()
                : config.getRemoveAbandonedTimeoutMillis();
        if (timeout != null) {
            dataSource.setRemoveAbandonedTimeoutMillis(timeout);
        }
        Boolean abandoned = config.getRemoveAbandoned() == null ? druidConfig.getRemoveAbandoned() : config.getRemoveAbandoned();
        if (abandoned != null) {
            dataSource.setRemoveAbandoned(abandoned);
        }
        Boolean logAbandoned = config.getLogAbandoned() == null ? druidConfig.getLogAbandoned() : config.getLogAbandoned();
        if (logAbandoned != null) {
            dataSource.setLogAbandoned(logAbandoned);
        }
        Integer queryTimeOut = config.getQueryTimeout() == null ? druidConfig.getQueryTimeout() : config.getQueryTimeout();
        if (queryTimeOut != null) {
            dataSource.setQueryTimeout(queryTimeOut);
        }
        Integer transactionQueryTimeout =
                config.getTransactionQueryTimeout() == null ? druidConfig.getTransactionQueryTimeout() : config.getTransactionQueryTimeout();
        if (transactionQueryTimeout != null) {
            dataSource.setTransactionQueryTimeout(transactionQueryTimeout);
        }
    }

    private void configureFilters(DruidDataSource dataSource, Properties properties, DataSourceProperty property) {
        List<Filter> proxyFilters = new ArrayList<>(2);
        String filters = properties.getProperty("druid.filters");
        if (StringUtils.isNotBlank(filters)) {
            addStatFilterIfNeeded(filters, properties, proxyFilters);
            addWallFilterIfNeeded(filters, property, proxyFilters);
            addSlf4jFilterIfNeeded(filters, proxyFilters);
        }
        if (this.applicationContext != null) {
            for (String filterId : druidConfig.getProxyFilters()) {
                proxyFilters.add(this.applicationContext.getBean(filterId, Filter.class));
            }
        }
        if (CollectionUtils.isNotEmpty(proxyFilters)) {
            dataSource.setProxyFilters(proxyFilters);
        }
    }

    private void addStatFilterIfNeeded(String filters, Properties properties, List<Filter> proxyFilters) {
        if (filters.contains(FILTER_TYPE_STAT)) {
            StatFilter statFilter = new StatFilter();
            statFilter.configFromProperties(properties);
            proxyFilters.add(statFilter);
        }
    }

    private void addWallFilterIfNeeded(String filters, DataSourceProperty property, List<Filter> proxyFilters) {
        if (filters.contains(FILTER_TYPE_WALL)) {
            WallConfig wallConfig = DruidWallConfigUtil.toWallConfig(
                    property.getDruid().getWall(),
                    druidConfig.getWall()
            );
            WallFilter wallFilter = new WallFilter();
            wallFilter.setConfig(wallConfig);
            proxyFilters.add(wallFilter);
        }
    }

    private void addSlf4jFilterIfNeeded(String filters, List<Filter> proxyFilters) {
        if (filters.contains(FILTER_TYPE_SLF4J)) {
            Slf4jLogFilter slf4jLogFilter = new Slf4jLogFilter();
            DruidSlf4jConfig slf4jConfig = druidConfig.getSlf4j();
            slf4jLogFilter.setStatementLogEnabled(slf4jConfig.getEnable());
            slf4jLogFilter.setStatementExecutableSqlLogEnable(slf4jConfig.getStatementExecutableSqlLogEnable());
            proxyFilters.add(slf4jLogFilter);
        }
    }

    private String parseDbType(String url) {
        String jdbcUrl = url.trim();
        if (jdbcUrl.startsWith(JDBC_P6SPY_PREFIX)) {
            jdbcUrl = url.replace(JDBC_P6SPY_PREFIX, JDBC_PREFIX);
        }
        return JdbcUtils.getDbType(jdbcUrl, null);
    }

}
