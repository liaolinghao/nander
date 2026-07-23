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
package wang.bigbird.domain.framework.server.rpc.core.support.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.config.spring.context.event.DubboBootstrapStopedEvent;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.server.rpc.core.support.holder.DubboAddressHolder;

/**
 * Dubbo服务状态监听器
 *
 * @author Bigbird
 */
@Slf4j
@Component
public class DubboStopListener implements ApplicationListener<DubboBootstrapStopedEvent> {

    @Override
    public void onApplicationEvent(DubboBootstrapStopedEvent event) {
        Object source = event.getSource();
        if (!(source instanceof DubboBootstrap)) {
            return;
        }
        DubboBootstrap dubboBootstrap = (DubboBootstrap) source;
        if (!dubboBootstrap.isStarted()) {
            // 仅服务提供者需要重置地址，纯消费者无地址无需操作
            boolean isProvider = !ApplicationModel.allProviderModels().isEmpty();
            if (isProvider) {
                DubboAddressHolder.resetAddress();
            }
        }
    }

}
