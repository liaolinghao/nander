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

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Nacos注册器
 *
 * @author Bigbird
 */
@Component
public class NacosRegisterHandler {

    @Value("${nacos.discovery.service-name}")
    private String serviceName;
    @Value("${nacos.discovery.group}")
    private String group;

    @Autowired
    private NamingService namingService;

    /**
     * 注册服务实例信息
     *
     * @param ip       服务IP
     * @param port     服务端口
     * @param metadata 服务元数据
     */
    public void registerInstance(String ip, int port, Map<String, String> metadata) throws NacosException {
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
        namingService.deregisterInstance(serviceName, group, ip, port);
    }

}
