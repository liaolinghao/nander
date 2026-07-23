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
package wang.bigbird.domain.framework.server.rpc.core.config.configuration;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Properties;

/**
 * RPC框架配置
 *
 * @author Bigbird
 */
@Slf4j
@ComponentScan("wang.bigbird.domain.framework.server.rpc.core")
@Configuration
public class RpcCoreConfiguration {

    @Value("${nacos.discovery.server-addr:}")
    private String serverAddr;
    @Value("${nacos.discovery.namespace:}")
    private String namespace;
    @Value("${nacos.discovery.username:nacos}")
    private String username;
    @Value("${nacos.discovery.password:nacos}")
    private String password;

    @PostConstruct
    public void init() {
        log.info("init core rpc framework.");
    }

    @Bean(destroyMethod = "shutDown")
    @ConditionalOnProperty(prefix = "nacos.discovery", name = "server-addr", matchIfMissing = false)
    public NamingService namingService() throws NacosException {
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        properties.put("namespace", namespace);
        // Nacos 认证配置（若未修改默认用户名密码，可省略，此处兼容配置）
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            properties.put("username", username);
            properties.put("password", password);
        }
        return NacosFactory.createNamingService(properties);
    }

}
