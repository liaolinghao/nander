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
package wang.bigbird.domain.framework.data.redis.config.configuration;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import wang.bigbird.domain.framework.data.redis.base.helper.PropertiesHelper;
import wang.bigbird.domain.framework.data.redis.config.property.RedisProperties;
import wang.bigbird.domain.framework.data.redis.support.factory.RedisTemplateFactory;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REDIS 配置
 *
 * @author Bigbird
 */
@Configuration
@Slf4j
@ComponentScan(basePackages = "wang.bigbird.domain.framework.data.redis")
@ConditionalOnProperty(
        prefix = "bigbird.data.redis",
        name = "enable",
        havingValue = "true",
        matchIfMissing = true
)
public class RedisConfiguration {

    @PostConstruct
    public void init() {
        log.info("Init redis framework.");
    }

    /**
     * redisson客户端
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedisProperties redisProperties,
                                         org.springframework.boot.autoconfigure.data.redis.RedisProperties springRedisProperties) {
        PropertiesHelper.combineRedisProperties(redisProperties, springRedisProperties);
        return buildRedissonClient(redisProperties);
    }

    /**
     * 以 redisson 为驱动的连接工厂
     */
    @Bean
    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redissonClient) {
        return new RedissonConnectionFactory(redissonClient);
    }

    /**
     * 构建 redisTemplate
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisTemplate redisTemplate(RedissonConnectionFactory redissonConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.string());
        template.setHashValueSerializer(RedisSerializer.string());
        template.setDefaultSerializer(RedisSerializer.string());
        template.setConnectionFactory(redissonConnectionFactory);
        return template;
    }

    /**
     * redisTemplate 工厂
     * 默认采用 jackson 序列化方式，不支持 value class 设置为 Object.class
     */
    @Bean
    public RedisTemplateFactory redisTemplateFactory(RedissonConnectionFactory redissonConnectionFactory) {
        return new RedisTemplateFactory(redissonConnectionFactory);
    }

    /**
     * 构建 redisson 客户端
     */
    private RedissonClient buildRedissonClient(RedisProperties redisProperties) {
        String redisUrlPrefix = "redis://";
        String addresses = redisProperties.getAddresses();
        String[] addressArray = addresses.split(",");
        Config config = new Config();
        if (addressArray.length == 1) {
            SingleServerConfig singleServerConfig = config.useSingleServer();
            singleServerConfig
                    .setAddress(redisUrlPrefix + addresses)
                    .setConnectionPoolSize(redisProperties.getConnectionPoolSize())
                    .setConnectionMinimumIdleSize(redisProperties.getConnectionMinimumIdleSize())
                    .setTimeout(redisProperties.getTimeout())
                    .setConnectTimeout(redisProperties.getConnectTimeout())
                    .setPassword(redisProperties.getPassword())
                    .setDatabase(redisProperties.getDatabase());
        } else {
            ClusterServersConfig clusterServersConfig = config.useClusterServers();
            List<String> addressList = Arrays.stream(addressArray)
                    .map(url -> redisUrlPrefix + url)
                    .collect(Collectors.toList());
            clusterServersConfig.setNodeAddresses(addressList);
            clusterServersConfig
                    .setMasterConnectionPoolSize(redisProperties.getConnectionPoolSize())
                    .setSlaveConnectionPoolSize(redisProperties.getConnectionPoolSize())
                    .setMasterConnectionMinimumIdleSize(redisProperties.getConnectionMinimumIdleSize())
                    .setSlaveConnectionMinimumIdleSize(redisProperties.getConnectionMinimumIdleSize())
                    .setTimeout(redisProperties.getTimeout())
                    .setConnectTimeout(redisProperties.getConnectTimeout())
                    .setPassword(redisProperties.getPassword());
        }
        config.setCodec(new StringCodec());
        return Redisson.create(config);
    }

}


