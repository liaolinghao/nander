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
package wang.bigbird.domain.framework.server.rpc.core.support.holder;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.rpc.Protocol;
import org.apache.dubbo.rpc.ProtocolServer;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.apache.dubbo.rpc.model.ProviderModel;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.springframework.util.ReflectionUtils;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.server.rpc.core.domain.pojo.DubboAddress;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Dubbo资源持有者，在服务启动或者重启时，可通过该持有者获取Dubbo占用的计算机资源
 *
 * @author Bigbird
 */
@Slf4j
public class DubboAddressHolder {

    /**
     * 存储 dubbo 协议地址（核心）
     */
    private static final AtomicReference<DubboAddress> DUBBO_ADDRESS = new AtomicReference<>();
    /**
     * 等待资源就绪的闭锁（替代休眠），用 AtomicReference 包装闭锁，支持原子替换
     */
    private static final AtomicReference<CountDownLatch> ADDRESS_LATCH =
            new AtomicReference<>(new CountDownLatch(1));

    public static void init() {
        try {
            // 从 ProtocolServer 获取（推荐，直接获取监听端口）
            boolean portInited = initFromProtocolServer();
            if (!portInited) {
                // 从 ProviderModel 获取
                initFromProviderModel();
            }
            // 端口就绪，释放闭锁
            if (DUBBO_ADDRESS.get() != null) {
                // 释放当前的闭锁
                CountDownLatch currentLatch = ADDRESS_LATCH.get();
                currentLatch.countDown();
            } else {
                throw new RuntimeException("Dubbo initialization failed.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Dubbo initialization failed.", e);
        }
    }

    /**
     * 等待资源就绪（带超时，外部线程调用）
     *
     * @param timeoutSeconds 等待秒数
     * @return 是否资源准备完毕
     */
    public static boolean waitForAddressReady(int timeoutSeconds) {
        try {
            // 先获取当前的闭锁实例，再等待
            CountDownLatch currentLatch = ADDRESS_LATCH.get();
            return currentLatch.await(timeoutSeconds, TimeUnit.SECONDS)
                    && getDubboAddress() != null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 获取 Dubbo 地址（外部线程调用）
     */
    public static DubboAddress getDubboAddress() {
        return DUBBO_ADDRESS.get();
    }

    /**
     * 更新地址（动态变更时调用）
     */
    public static void updateAddress(String protocol, DubboAddress address) {
        if (CommonConstants.DUBBO_PROTOCOL.equals(protocol)) {
            DUBBO_ADDRESS.set(address);
        }
    }

    /**
     * 重置地址（Dubbo 重启时调用）
     */
    public static void resetAddress() {
        DUBBO_ADDRESS.set(null);
        // 原子替换闭锁（核心：创建新的闭锁实例，替换旧的）
        ADDRESS_LATCH.set(new CountDownLatch(1));
    }

    /**
     * 从 ProtocolServer 获取（最准确）
     */
    private static boolean initFromProtocolServer() {
        try {
            ExtensionLoader<Protocol> loader = ExtensionLoader.getExtensionLoader(Protocol.class);
            Protocol protocolWrapper = loader.getExtension(CommonConstants.DUBBO_PROTOCOL);
            DubboProtocol dubboProtocol = getRealProtocolFromWrapper(protocolWrapper);
            List<ProtocolServer> servers = dubboProtocol.getServers();
            for (ProtocolServer server : servers) {
                String address = server.getAddress();
                DubboAddress dubboAddress = DubboAddress.parse(address);
                DUBBO_ADDRESS.set(dubboAddress);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("InitFromProtocolServer：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 递归拆包，直到获取最终的DubboProtocol
     *
     * @param wrapperProtocol
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static DubboProtocol getRealProtocolFromWrapper(Protocol wrapperProtocol) throws NoSuchFieldException, IllegalAccessException {
        // 递归穿透所有 Wrapper（防止多层包装）
        if (wrapperProtocol instanceof DubboProtocol) {
            return (DubboProtocol) wrapperProtocol;
        }
        // 获取 QosProtocolWrapper 中的 "protocol" 成员变量
        Field protocolField = wrapperProtocol.getClass().getDeclaredField("protocol");
        // 突破私有访问限制
        ReflectionUtils.makeAccessible(protocolField);
        Protocol innerProtocol = (Protocol) protocolField.get(wrapperProtocol);
        // 递归检查内部实例是否还是 Wrapper
        return getRealProtocolFromWrapper(innerProtocol);
    }

    /**
     * 从 ProviderModel 获取（兜底）
     */
    private static void initFromProviderModel() {
        // 获取所有 ProviderModel（服务提供者元数据）
        Collection<ProviderModel> providerModels = ApplicationModel.allProviderModels();
        if (CollectionUtils.isNotEmpty(providerModels)) {
            for (ProviderModel providerModel : providerModels) {
                // 获取服务暴露的 URL（格式：dubbo://192.168.1.100:20880/com.xxx.Service?xxx）
                URL exportUrl = providerModel.getStatedUrl().get(0).getProviderUrl();
                if (exportUrl != null && CommonConstants.DUBBO_PROTOCOL.equals(exportUrl.getProtocol())) {
                    String address = exportUrl.getAddress();
                    DubboAddress dubboAddress = DubboAddress.parse(address);
                    DUBBO_ADDRESS.set(dubboAddress);
                    // 只取第一个 dubbo 协议地址
                    break;
                }
            }
        }
    }

}
