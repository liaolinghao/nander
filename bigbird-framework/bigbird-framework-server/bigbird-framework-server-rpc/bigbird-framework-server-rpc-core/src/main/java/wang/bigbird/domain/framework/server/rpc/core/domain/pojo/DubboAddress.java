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
package wang.bigbird.domain.framework.server.rpc.core.domain.pojo;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;

import java.util.Objects;

/**
 * Dubbo地址信息
 *
 * @author Bigbird
 */
@Data
public final class DubboAddress {

    private final static int MAX_PORT = 65535;

    /**
     * 监听IP（如 192.168.1.100、0.0.0.0、localhost）
     */
    private final String ip;
    /**
     * 监听端口
     */
    private final int port;

    private DubboAddress(String ip, int port) {
        // 校验参数有效性
        this.ip = Objects.requireNonNull(ip, "IP cannot be empty.");
        if (port <= 0 || port > MAX_PORT) {
            throw new IllegalArgumentException("Dubbo port must be between 1-65535: " + port);
        }
        this.port = port;
    }

    public static DubboAddress of(String ip, int port) {
        return new DubboAddress(ip, port);
    }

    /**
     * 解析地址字符串（如 "192.168.1.100:20880"）
     *
     * @param address
     * @return dubbo地址对象
     */
    public static DubboAddress parse(String address) {
        if (StringUtils.isBlank(address) || !address.contains(CommonConstants.COLON)) {
            throw new IllegalArgumentException("Invalid Dubbo address format: " + address);
        }
        String[] parts = address.split(CommonConstants.COLON);
        String ip = parts[0];
        int port = Integer.parseInt(parts[1]);
        return of(ip, port);
    }

    /**
     * 获取完整地址（IP:Port）
     *
     * @return 完整地址
     */
    public String getFullAddress() {
        return ip + CommonConstants.COLON + port;
    }

    @Override
    public String toString() {
        return getFullAddress();
    }

}
