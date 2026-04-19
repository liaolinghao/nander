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
package wang.bigbird.domain.framework.core.base.util.url.proxy;

import wang.bigbird.domain.framework.core.base.util.StringUtils;

import java.io.Serializable;

/**
 * Http代理对象
 *
 * @author Bigbird
 */
public class HttpProxy implements Serializable {

    private static final long serialVersionUID = -551728215328765792L;

    private String proxy = "http";
    private String host;
    private int port;
    private String user;
    private String password;

    public HttpProxy() {

    }

    public HttpProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public HttpProxy(String host, int port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public String getProxy() {
        return StringUtils.processNullStr(proxy, "http");
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getHost() {
        return StringUtils.processNullStr(host);
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return StringUtils.processNullStr(user);
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return StringUtils.processNullStr(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 代理主机不是IP，代表这是隧道模式，隧道模式会自行切换IP，程序不需要重新获取
     *
     * @return
     */
    public boolean isChannel() {
        return !StringUtils.isIp(host);
    }

    @Override
    public int hashCode() {
        return host.hashCode() + port;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HttpProxy other = (HttpProxy) obj;
        if (host == null) {
            if (other.getHost() != null) {
                return false;
            }
        } else if (!host.equals(other.getHost())) {
            return false;
        }
        if (port != other.getPort()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
