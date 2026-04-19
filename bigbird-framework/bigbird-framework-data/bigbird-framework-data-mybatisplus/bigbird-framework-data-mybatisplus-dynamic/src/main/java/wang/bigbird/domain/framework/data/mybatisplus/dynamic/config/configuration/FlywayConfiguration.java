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
package wang.bigbird.domain.framework.data.mybatisplus.dynamic.config.configuration;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 采用flyway支持多数据源脚本执行的配置
 *
 * @author Bigbird
 */
@Slf4j
@Configuration
@EnableTransactionManagement
@ConditionalOnProperty(name = "spring.flyway.type", havingValue = "dynamic", matchIfMissing = true)
public class FlywayConfiguration {

    @Autowired
    private DataSource dataSource;

    private static final String SQL_LOCATION = "classpath:db/";

    @Bean
    public void migrate() {
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        Map<String, DataSource> dataSources = ds.getCurrentDataSources();
        dataSources.forEach((k, v) -> {
            log.info("Execute the script file corresponding to the data source: {}.", k);
            doMigrate(k, v);
        });
    }

    /**
     * 自动执行对应数据源脚本
     *
     * @param datasourceName 数据源名称
     * @param dataSource     数据源
     */
    private void doMigrate(String datasourceName, DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(SQL_LOCATION + datasourceName)
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
    }

}
