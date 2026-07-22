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
package wang.bigbird.domain.framework.server.rpc.core.support.handler;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;

/**
 * Nacos注册器
 *
 * @author Bigbird
 */
@Component
public class NacosRegisterHandler {

    @Value("${nacos.discovery.server-addr}")
    private String serverAddr;
    @Value("${nacos.discovery.service-name}")
    private String serviceName;
    @Value("${nacos.discovery.group}")
    private String group;
    @Value("${nacos.discovery.namespace}")
    private String namespace;
    @Value("${nacos.discovery.username:nacos}")
    private String username;
    @Value("${nacos.discovery.password:nacos}")
    private String password;

    /**
     * Nacos 命名服务实例（核心操作类）
     */
    private NamingService namingService;

    /**
     * 初始化 NamingService 实例（解决 Nacos 认证问题）
     */
    private void initNamingService() throws NacosException {
        if (namingService != null) {
            return;
        }
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        properties.put("namespace", namespace);
        // Nacos 认证配置（若未修改默认用户名密码，可省略，此处兼容配置）
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            properties.put("username", username);
            properties.put("password", password);
        }
        namingService = NacosFactory.createNamingService(properties);
    }

    /**
     * 注册服务实例信息
     *
     * @param ip       服务IP
     * @param port     服务端口
     * @param metadata 服务元数据
     */
    public void registerInstance(String ip, int port, Map<String, String> metadata) throws NacosException {
        initNamingService();
        // 构建 Nacos实例对象
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(port);
        instance.setHealthy(true);
        instance.setWeight(1.0);
        instance.setMetadata(metadata);
        // 注册实例到 Nacos（指定服务名和分组）
        namingService.registerInstance(serviceName, group, instance);
    }

    /**
     * 注销服务实例信息（应用关闭时执行）
     *
     * @param ip   服务IP
     * @param port 服务端口
     */
    public void deregisterInstance(String ip, int port) throws NacosException {
        if (namingService == null) {
            return;
        }
        namingService.deregisterInstance(serviceName, group, ip, port);
    }

}
