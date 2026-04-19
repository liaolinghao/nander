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
package wang.bigbird.domain.framework.cache.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import wang.bigbird.domain.framework.cache.base.enums.CacheTypeEnum;

import java.time.Duration;


/**
 * 缓存统一配置
 *
 * @author Bigbird
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.cache")
public class CacheProperties {

    /**
     * Cache 类型
     */
    private CacheTypeEnum type;

    private final Caffeine caffeine = new Caffeine();

    private final Redis redis = new Redis();

    /**
     * Caffeine 配置
     */
    @Data
    public static class Caffeine {

        /**
         * 过期时间
         */
        private Duration timeToLive = Duration.ofDays(1);

        /**
         * 缓存特征申明格式：maximumSize=200,expireAfterWrite=300s,recordStats
         */
        private String spec;

    }


    /**
     * Redis 配置
     */
    @Data
    public static class Redis {

        /**
         * 过期时间，确定对象在Redis缓存中的最大生存期。
         * 缓存中对象的生存时间到期后，无论请求的频率如何，都将删除它们
         * 也就是说缓存数据的访问不会导致数据的生存期延长
         */
        private Duration timeToLive = Duration.ofDays(1);

        /**
         * 确定两次请求对象之间可以经过的最长时间。
         * 如果这段时间没有请求，对象将自动从内存缓存中删除。
         * 此时Redis缓存中仍然有值，但是数据不从redis中获取，
         * 而是进入业务方法获取值后更新到redis缓存中。
         * 该参数仅在Spring缓存的Redisson实现中存在。
         */
        private Duration maxIdleTime = Duration.ZERO;

    }

}
