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

import org.apache.dubbo.common.serialize.kryo.utils.KryoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.core.base.tool.pageable.PageData;
import wang.bigbird.domain.framework.core.base.tool.pageable.param.Order;
import wang.bigbird.domain.framework.core.base.tool.pageable.param.Pageable;
import wang.bigbird.domain.framework.server.rpc.core.config.property.RpcProperties;
import wang.bigbird.domain.framework.server.rpc.core.support.annotation.KryoSerializable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Kryo注册器，将RPC接口涉及的传输对象进行注册，
 * 防止发生Encountered unregistered class ID错误
 *
 * @author Bigbird
 */
@Component
public class KryoAutoRegisterRunner implements CommandLineRunner {

    @Autowired
    private RpcProperties rpcProperties;

    @Override
    public void run(String... args) throws Exception {
        // 扫描指定包下带 @KryoSerializable 的类
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(KryoSerializable.class));
        List<Class<?>> allKryoClasses = new ArrayList<>();
        // 1. 收集所有匹配类，不立刻注册
        for (String basePackage : rpcProperties.getScanKryoSerializablePackages()) {
            Set<BeanDefinition> candidates = scanner.findCandidateComponents(basePackage);
            for (BeanDefinition bd : candidates) {
                String className = bd.getBeanClassName();
                Class<?> clazz = Class.forName(className);
                allKryoClasses.add(clazz);
            }
        }
        // 2. 按类全限定名升序排序，全局统一顺序
        allKryoClasses.sort(Comparator.comparing(Class::getName));
        // 3. 有序执行注册，所有服务顺序完全一致
        for (Class<?> clazz : allKryoClasses) {
            KryoUtils.register(clazz);
        }
        // 4. 注册分页相关类
        KryoUtils.register(PageData.class);
        KryoUtils.register(Pageable.class);
        KryoUtils.register(Order.class);
    }

}
