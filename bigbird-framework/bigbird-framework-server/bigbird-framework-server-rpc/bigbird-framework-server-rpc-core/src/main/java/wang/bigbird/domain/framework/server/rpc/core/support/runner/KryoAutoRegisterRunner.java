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
package wang.bigbird.domain.framework.server.rpc.core.support.runner;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.serialize.kryo.utils.KryoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.core.base.tool.pageable.PageData;
import wang.bigbird.domain.framework.core.base.tool.pageable.param.Order;
import wang.bigbird.domain.framework.core.base.tool.pageable.param.Pageable;
import wang.bigbird.domain.framework.server.rpc.core.config.property.RpcProperties;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Kryo注册器，将RPC接口涉及的传输对象进行注册，
 * 防止发生Encountered unregistered class ID错误
 *
 * @author Bigbird
 */
@Slf4j
@Component
public class KryoAutoRegisterRunner implements CommandLineRunner {

    @Autowired
    private RpcProperties rpcProperties;

    @Override
    public void run(String... args) throws Exception {
        // 扫描指定包下实现了Serializable的类
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(Serializable.class));
        TreeSet<Class<?>> allKryoClasses = new TreeSet<>(Comparator.comparing(Class::getName));
        // 收集所有匹配类，不立刻注册
        for (String basePackage : rpcProperties.getScanKryoSerializablePackages()) {
            Set<BeanDefinition> candidates = scanner.findCandidateComponents(basePackage);
            for (BeanDefinition bd : candidates) {
                String className = bd.getBeanClassName();
                Class<?> clazz = Class.forName(className);
                allKryoClasses.add(clazz);
            }
        }
        // 有序执行注册，所有服务顺序完全一致
        for (Class<?> clazz : allKryoClasses) {
            log.info("register class:{}.", clazz.getName());
            KryoUtils.register(clazz);
        }
        // 注册分页相关类
        KryoUtils.register(PageData.class);
        KryoUtils.register(Pageable.class);
        KryoUtils.register(Order.class);
    }

}
