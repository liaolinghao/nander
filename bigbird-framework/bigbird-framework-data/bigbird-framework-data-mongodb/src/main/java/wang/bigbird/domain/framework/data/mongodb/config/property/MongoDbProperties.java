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
package wang.bigbird.domain.framework.data.mongodb.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * MongoDB 属性
 *
 * @author Bigbird
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "bigbird.data.mongodb")
public class MongoDbProperties {

    /**
     * 加解密密钥
     */
    private String key = "bigbird";
    /**
     * 连接信息相关，实例 uri
     */
    private String uri;
    /**
     * 连接信息相关，数据库名称
     */
    private String database;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private char[] password;

    /**
     * socket读超时时间，单位：毫秒，值为0意味着不会超时
     */
    private Integer readTimeout = 0;
    /**
     * 连接超时时间，单位：毫秒
     */
    private Integer connectTimeout = 10000;

    /**
     * 连接池空闲时保持的最小连接数
     */
    private Integer connectionPoolMinSize = 5;
    /**
     * 连接池允许的最大连接数
     */
    private Integer connectionPoolMaxSize = 50;
    /**
     * 线程等待连接变为可用的最长时间，单位：毫秒，值为0意味着它不会等待，负值意味着它将无限期地等待。
     */
    private Integer connectionPoolMaxWaitTime = 3 * 60 * 1000;

}
