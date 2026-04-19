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
package wang.bigbird.domain.framework.data.mongodb.config.configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ConnectionPoolSettings;
import com.mongodb.connection.SocketSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.data.mongodb.base.helper.PropertiesHelper;
import wang.bigbird.domain.framework.data.mongodb.config.property.MongoDbProperties;

import java.util.concurrent.TimeUnit;


/**
 * MongoDB 配置
 *
 * @author Bigbird
 */
@Configuration
@Slf4j
@ComponentScan("wang.bigbird.domain.framework.data.mongodb")
public class MongoDbConfiguration {

    /**
     * MongoDB 客户端
     */
    @Bean
    public MongoClient mongo(MongoDbProperties mongoDbProperties, org.springframework.boot.autoconfigure.mongo.MongoProperties springMongoProperties) {
        PropertiesHelper.combineMongoProperties(mongoDbProperties, springMongoProperties);
        return buildMongoClient(mongoDbProperties);
    }

    /**
     * MongoTemplate
     * 如果保留该方法名作为Bean名称，需要开启Bean覆盖，否则会报错
     *
     * @SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
     * @SpringBootApplication(properties = "spring.main.allow-bean-definition-overriding=true")
     * 或者在配置文件中设置
     * spring.main.allow-bean-definition-overriding=true
     * 或yml配置文件
     * spring:
     * main:
     * allow-bean-definition-overriding: true
     */
    @Bean
    @Primary
    public MongoTemplate mongoTemplate(MongoClient mongoClient, MongoDbProperties mongoDbProperties) {
        String database = mongoDbProperties.getDatabase();
        return new MongoTemplate(mongoClient, database);
    }

    private MongoClient buildMongoClient(MongoDbProperties mongoDbProperties) {
        MongoClientSettings.Builder builder = MongoClientSettings.builder();
        // 连接 uri
        builder.applyConnectionString(new ConnectionString(mongoDbProperties.getUri()));
        if (StringUtils.isNotBlank(mongoDbProperties.getUsername()) && StringUtils.isNotBlank(mongoDbProperties.getDatabase()) && mongoDbProperties.getPassword() != null) {
            builder.credential(MongoCredential.createCredential(mongoDbProperties.getUsername(), mongoDbProperties.getDatabase(), mongoDbProperties.getPassword()));
        }
        // 连接池配置
        ConnectionPoolSettings connectionPoolSettings = ConnectionPoolSettings.builder()
                .minSize(mongoDbProperties.getConnectionPoolMinSize())
                .maxSize(mongoDbProperties.getConnectionPoolMaxSize())
                .maxWaitTime(mongoDbProperties.getConnectionPoolMaxWaitTime(), TimeUnit.MILLISECONDS)
                .build();
        builder.applyToConnectionPoolSettings(b -> b.applySettings(connectionPoolSettings));
        // 连接配置
        SocketSettings socketSettings = SocketSettings.builder()
                .readTimeout(mongoDbProperties.getReadTimeout(), TimeUnit.MILLISECONDS)
                .connectTimeout(mongoDbProperties.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .build();
        builder.applyToSocketSettings(b -> b.applySettings(socketSettings));
        MongoClientSettings settings = builder.build();
        return MongoClients.create(settings);
    }

}
