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
package wang.bigbird.domain.framework.data.redis.base.helper;

import org.apache.commons.collections4.CollectionUtils;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.util.CryptUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.data.redis.config.property.RedisProperties;

import java.util.List;

/**
 * 属性设置器
 *
 * @author Bigbird
 */
public class PropertiesHelper {

    /**
     * 合并redis配置
     * <p>
     * 配置加载优先级如下：
     * <p>
     * 1、自定义配置优先
     * <p>
     * 2、spring原生配置作为候补
     *
     * @param redisProperties       自定义redis属性
     * @param springRedisProperties spring原生的redis配置
     */
    public static void combineRedisProperties(RedisProperties redisProperties, org.springframework.boot.autoconfigure.data.redis.RedisProperties springRedisProperties) {
        redisProperties.setAddresses(loadAddresses(redisProperties, springRedisProperties));
        redisProperties.setPassword(loadPassword(redisProperties, springRedisProperties));
        redisProperties.setDatabase(loadDatabase(redisProperties, springRedisProperties));
        redisProperties.setConnectTimeout(loadConnectTimeout(redisProperties, springRedisProperties));
        redisProperties.setConnectionPoolSize(loadConnectionPoolSize(redisProperties, springRedisProperties));
        redisProperties.setConnectionMinimumIdleSize(loadConnectionMinimumIdleSize(redisProperties, springRedisProperties));
    }

    private static Integer loadConnectionMinimumIdleSize(RedisProperties redisProperties, org.springframework.boot.autoconfigure.data.redis.RedisProperties springRedisProperties) {
        if (redisProperties.getConnectionMinimumIdleSize() == null) {
            org.springframework.boot.autoconfigure.data.redis.RedisProperties.Lettuce lettuce = springRedisProperties.getLettuce();
            if (null != lettuce) {
                org.springframework.boot.autoconfigure.data.redis.RedisProperties.Pool pool = lettuce.getPool();
                if (null != pool) {
                    return pool.getMinIdle();
                }
            }
        } else {
            return redisProperties.getConnectionMinimumIdleSize();
        }
        return 2;
    }

    private static Integer loadConnectionPoolSize(RedisProperties redisProperties, org.springframework.boot.autoconfigure.data.redis.RedisProperties springRedisProperties) {
        if (redisProperties.getConnectionPoolSize() == null) {
            org.springframework.boot.autoconfigure.data.redis.RedisProperties.Lettuce lettuce = springRedisProperties.getLettuce();
            if (null != lettuce) {
                org.springframework.boot.autoconfigure.data.redis.RedisProperties.Pool pool = lettuce.getPool();
                if (null != pool) {
                    return pool.getMaxActive();
                }
            }
        } else {
            return redisProperties.getConnectionPoolSize();
        }
        return 10;
    }

    private static Integer loadConnectTimeout(RedisProperties redisProperties, org.springframework.boot.autoconfigure.data.redis.RedisProperties springRedisProperties) {
        if (redisProperties.getConnectTimeout() == null) {
            if (springRedisProperties.getTimeout() != null) {
                return Long.valueOf(springRedisProperties.getTimeout().toMillis()).intValue();
            }
        } else {
            return redisProperties.getConnectTimeout();
        }
        return 10000;
    }

    private static Integer loadDatabase(RedisProperties redisProperties, org.springframework.boot.autoconfigure.data.redis.RedisProperties springRedisProperties) {
        if (redisProperties.getDatabase() == null) {
            return springRedisProperties.getDatabase();
        } else {
            return redisProperties.getDatabase();
        }
    }

    private static String loadPassword(RedisProperties redisProperties, org.springframework.boot.autoconfigure.data.redis.RedisProperties springRedisProperties) {
        if (StringUtils.isBlank(redisProperties.getPassword())) {
            if (StringUtils.isNotBlank(springRedisProperties.getPassword())) {
                return springRedisProperties.getPassword();
            }
        } else {
            return CryptUtils.decrypt(redisProperties.getPassword(), redisProperties.getKey());
        }
        return null;
    }

    private static String loadAddresses(RedisProperties redisProperties, org.springframework.boot.autoconfigure.data.redis.RedisProperties springRedisProperties) {
        if (StringUtils.isBlank(redisProperties.getAddresses())) {
            org.springframework.boot.autoconfigure.data.redis.RedisProperties.Cluster cluster = springRedisProperties.getCluster();
            if (null != cluster && CollectionUtils.isNotEmpty(cluster.getNodes())) {
                // 集群
                List<String> nodes = cluster.getNodes();
                return org.apache.commons.lang3.StringUtils.join(nodes, CommonConstants.COMMA);
            } else if (StringUtils.isNotBlank(springRedisProperties.getHost())) {
                // 单机
                return StringUtils.joinStr(springRedisProperties.getHost(), CommonConstants.COLON, springRedisProperties.getPort());
            }
        } else {
            return redisProperties.getAddresses();
        }
        return "127.0.0.1:6379";
    }

}
