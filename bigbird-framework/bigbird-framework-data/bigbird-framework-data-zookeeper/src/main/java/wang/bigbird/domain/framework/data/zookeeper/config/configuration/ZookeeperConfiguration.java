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
package wang.bigbird.domain.framework.data.zookeeper.config.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import wang.bigbird.domain.framework.data.zookeeper.base.enums.AuthenticationTypeEnum;
import wang.bigbird.domain.framework.data.zookeeper.config.property.ZookeeperProperties;

import javax.annotation.PostConstruct;

/**
 * Zookeeper 配置
 *
 * @author Bigbird
 */
@Configuration
@Slf4j
@ComponentScan(basePackages = "wang.bigbird.domain.framework.data.zookeeper")
@ConditionalOnProperty(
        prefix = "bigbird.data.zookeeper",
        name = "enable",
        havingValue = "true",
        matchIfMissing = true
)
public class ZookeeperConfiguration {

    @PostConstruct
    public void init() {
        log.info("Init zookeeper framework.");
    }

    /**
     * Zookeeper client
     */
    @Bean(destroyMethod = "close")
    public CuratorFramework curatorFramework(ZookeeperProperties zookeeperProperties) {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(zookeeperProperties.getAddresses())
                .namespace(zookeeperProperties.getNamespace())
                .sessionTimeoutMs(zookeeperProperties.getSessionTimeout())
                .connectionTimeoutMs(zookeeperProperties.getConnectTimeout())
                .retryPolicy(zookeeperProperties.getRetry().getRetryPolicy());
        if (zookeeperProperties.getAuthentication().getType() != AuthenticationTypeEnum.world) {
            builder.authorization(zookeeperProperties.getAuthentication().getScheme(), zookeeperProperties.getAuthentication().getAuth());
        }
        CuratorFramework curatorFramework = builder.build();
        curatorFramework.start();
        return curatorFramework;
    }

}


