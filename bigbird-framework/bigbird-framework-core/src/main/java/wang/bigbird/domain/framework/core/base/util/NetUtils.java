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
package wang.bigbird.domain.framework.core.base.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * 网络操作工具类
 *
 * @author Bigbird
 */
@Slf4j
public class NetUtils {

    public static final String ANYHOST = "0.0.0.0";

    public static final String LOCALHOST = "127.0.0.1";

    public static final int MAX_PORT = 65535;

    private static final int RND_PORT_START = 30000;

    private static final int RND_PORT_RANGE = 10000;

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    /**
     * 解析域名对应的IP
     *
     * @param domain 域名
     * @return IP
     */
    public static String parseDomainIP(String domain) {
        try {
            return InetAddress.getByName(domain).getHostAddress();
        } catch (Exception e) {
            log.error("ParseDomainIP:", e);
        }
        return "";
    }

    /**
     * 获取30000以上的随机端口
     *
     * @return 30000以上的随机端口
     */
    public static int getRandomPort() {
        return RND_PORT_START + RANDOM.nextInt(RND_PORT_RANGE);
    }

    /**
     * 获取可用端口
     *
     * @return 可用端口
     */
    public static int getAvailablePort() {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket();
            ss.bind(null);
            return ss.getLocalPort();
        } catch (IOException e) {
            return getRandomPort();
        } finally {
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 获取默认IP
     *
     * @return ip
     */
    public static String getIp() {
        return getIp(null);
    }

    /**
     * 获取指定网卡对应IP
     *
     * @param interfaceName 网卡名称
     * @return ip
     */
    public static String getIp(String interfaceName) {
        String ip;
        try {
            List<String> ipList = getHostAddress(interfaceName);
            ip = (!ipList.isEmpty()) ? ipList.get(0) : "";
        } catch (Exception ex) {
            ip = "";
            log.error("GetIp:", ex);
        }
        return ip;
    }

    /**
     * 获取机器码
     */
    public static byte[] getMachineNum() {
        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            if (network != null) {
                byte[] mac = network.getHardwareAddress();
                if (null != mac) {
                    return mac;
                }
            }
        } catch (Exception e) {
            log.error("GetMachineNum:", e);
        }
        return null;
    }

    /**
     * 是否有效地址
     *
     * @param address
     * @return
     */
    private static boolean isValidAddress(InetAddress address) {
        if (address == null || address.isLoopbackAddress()) {
            return false;
        }
        String name = address.getHostAddress();
        return (name != null && !ANYHOST.equals(name) && !LOCALHOST.equals(name) && IP_PATTERN.matcher(name).matches());
    }

    /**
     * 获取已激活网卡的IP地址
     *
     * @param interfaceName 可指定网卡名称，null则获取全部
     * @return IP地址集合
     */
    private static List<String> getHostAddress(String interfaceName) throws SocketException {
        List<String> ipList = new ArrayList<String>(5);
        if (interfaceName != null) {
            interfaceName = interfaceName.trim();
        }
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            if (ni.isLoopback()) {
                // 排除loopback类型地址
                continue;
            }
            Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
            if (inetAddresses == null) {
                continue;
            }
            while (inetAddresses.hasMoreElements()) {
                InetAddress address = inetAddresses.nextElement();
                if (!isValidAddress(address)) {
                    continue;
                }
                String hostAddress = address.getHostAddress();
                if (null == interfaceName) {
                    ipList.add(hostAddress);
                } else if (interfaceName.equals(ni.getDisplayName())) {
                    ipList.add(hostAddress);
                }
            }
        }
        return ipList;
    }
}
