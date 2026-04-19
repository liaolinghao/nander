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
package wang.bigbird.domain.framework.data.redis.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Redis properties
 *
 * @author Bigbird
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "bigbird.data.redis")
public class RedisProperties {

    /**
     * 加解密密钥
     */
    private String key = "bigbird";
    /**
     * 节点地址, 逗号分隔
     */
    private String addresses;
    /**
     * 密码
     */
    private String password;
    /**
     * 库编号（单机版可用）
     */
    private Integer database;
    /**
     * 命令等待超时，单位：毫秒
     */
    private Integer timeout = 10000;
    /**
     * 连接超时，单位：毫秒
     */
    private Integer connectTimeout;
    /**
     * 节点连接池大小
     */
    private Integer connectionPoolSize;
    /**
     * 节点最小空闲连接数
     */
    private Integer connectionMinimumIdleSize;

}
