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
package wang.bigbird.domain.framework.data.zookeeper.config.property;

import lombok.Data;
import org.apache.curator.RetryPolicy;
import org.apache.curator.retry.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.util.CryptUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.data.zookeeper.base.enums.AuthenticationTypeEnum;
import wang.bigbird.domain.framework.data.zookeeper.base.enums.RetryPolicyTypeEnum;

import static wang.bigbird.domain.framework.data.zookeeper.base.enums.AuthenticationTypeEnum.world;
import static wang.bigbird.domain.framework.data.zookeeper.base.enums.RetryPolicyTypeEnum.ExponentialBackoff;

/**
 * Zookeeper 属性
 *
 * @author Bigbird
 */
@Validated
@Data
@Configuration
@ConfigurationProperties(prefix = "bigbird.data.zookeeper")
public class ZookeeperProperties {

    /**
     * 节点地址，逗号分隔
     */
    private String addresses = "127.0.0.1:2181";
    /**
     * 命名空间
     */
    private String namespace = "bigbird-framework";
    /**
     * 会话超时时间，单位：毫秒
     */
    private Integer sessionTimeout = 5000;
    /**
     * 连接超时，单位：毫秒
     */
    private Integer connectTimeout = 5000;

    private final Retry retry = new Retry();

    private final Authentication authentication = new Authentication();

    /**
     * 重连策略配置
     */
    @Data
    public static class Retry {

        /**
         * 重连策略
         */
        private RetryPolicyTypeEnum type = ExponentialBackoff;

        /**
         * 重连间隔时间，以秒为单位
         */
        int retryTime = 1;
        /**
         * 最大重连间隔时间，以秒为单位
         */
        int maxSleepTime = 30;
        /**
         * 最大重连次数
         */
        int maxRetries = 10;
        /**
         * 总等待时间，以秒为单位
         */
        int retryUntilElapsed = 10;

        public RetryPolicy getRetryPolicy() {
            switch (type) {
                case OneTime:
                    // retryTime秒后重连一次，只重连1次
                    return new RetryOneTime(retryTime * 1000);
                case NTimes:
                    // 每retryTime秒重连一次，重连maxRetries次
                    return new RetryNTimes(maxRetries, retryTime * 1000);
                case UntilElapsed:
                    // 每retryTime秒重连一次，总等待时间超过retryUntilElapsed秒后停止重连
                    return new RetryUntilElapsed(retryUntilElapsed * 1000,
                            retryTime * 1000);
                case ExponentialBackoff:
                    // baseSleepTimeMs * Math.max(1, random.nextInt(1 << (retryCount+
                    // 1)))
                    return new ExponentialBackoffRetry(retryTime * 1000,
                            maxRetries);
                case BoundedExponentialBackoff:
                    return new BoundedExponentialBackoffRetry(retryTime * 1000, maxSleepTime * 1000, maxRetries);
                default:
                    throw new IllegalArgumentException("Invalid retry policy type.");
            }
        }

    }

    /**
     * 认证策略配置
     */
    @Data
    public static class Authentication {
        /**
         * 加解密密钥
         */
        private String key = "bigbird";
        /**
         * 认证策略
         */
        private AuthenticationTypeEnum type = world;
        /**
         * 用户名
         */
        private String username = "";
        /**
         * 密码
         */
        private String password = "";

        public String getScheme() {
            return type.name();
        }

        public byte[] getAuth() {
            switch (type) {
                case world:
                    return null;
                case auth:
                case digest:
                case ip:
                    return StringUtils.joinStr(username, CommonConstants.COLON, CryptUtils.decrypt(password, key)).getBytes();
                default:
                    throw new IllegalArgumentException("Invalid authentication policy type.");
            }
        }

    }

}
