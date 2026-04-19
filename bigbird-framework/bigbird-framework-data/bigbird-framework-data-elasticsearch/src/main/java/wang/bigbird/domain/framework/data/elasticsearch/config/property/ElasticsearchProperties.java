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
package wang.bigbird.domain.framework.data.elasticsearch.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * Elasticsearch properties
 *
 * @author Bigbird
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "bigbird.data.elasticsearch")
public class ElasticsearchProperties {

    /**
     * 加解密密钥
     */
    private String key = "bigbird";
    /**
     * 基本设置：默认为http，使用searchguard后为https
     */
    private String scheme;
    /**
     * 基本设置：连接地址
     */
    private String addresses;
    /**
     * 基本设置：用户名
     */
    private String username;
    /**
     * 基本设置：密码
     */
    private String password;

    /**
     * 安全认证：searchguard:truststore.jks生成的密码
     */
    private String truststorePassword;
    /**
     * 安全认证：searchguard:truststore.jks的路径
     */
    private Resource truststorePath;

    /**
     * 网络设置：连接超时时间，单位：毫秒
     */
    private Integer connectTimeout;
    /**
     * 网络设置：socket超时时间，单位：毫秒
     */
    private Integer socketTimeout = 600000;
    /**
     * 网络设置：从连接池获取连接超时时间，单位：毫秒
     */
    private Integer connectionRequestTimeout = 60000;
    /**
     * 网络设置：最大连接数
     */
    private Integer maxConnectNum = 200;
    /**
     * 网络设置：最大路由连接数
     */
    private Integer maxConnectPerRoute = 100;
    /**
     * 网络设置：最大重试超时时间，单位：毫秒
     */
    private Integer maxRetryTimeout = 600000;

    /**
     * 批量操作设置：每添加几个request，执行一次bulk操作
     */
    private Integer bulkActions = 2000;
    /**
     * 批量操作设置：达到几M的请求大小时，执行一次bulk操作
     */
    private Integer bulkSize = 5;
    /**
     * 批量操作设置：每几秒执行一次bulk操作
     */
    private Integer bulkFlushInterval = 5;
    /**
     * 批量操作设置：bulk的并发线程数
     */
    private Integer bulkConcurrentRequests = 2;
}
