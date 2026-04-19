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
package wang.bigbird.domain.framework.data.rabbitmq.base.helper;

import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import wang.bigbird.domain.framework.core.base.util.CryptUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.data.rabbitmq.config.property.RabbitmqProperties;

/**
 * 属性设置器
 *
 * @author Bigbird
 */
public class PropertiesHelper {

    /**
     * 合并rabbitmq配置
     * <p>
     * 配置加载优先级如下：
     * <p>
     * 1、自定义配置优先
     * <p>
     * 2、spring原生配置作为候补
     *
     * @param rabbitmqProperties       自定义rabbitmq属性
     * @param springRabbitmqProperties spring原生的rabbitmq配置
     */
    public static void combineRabbitmqProperties(RabbitmqProperties rabbitmqProperties, RabbitProperties springRabbitmqProperties) {
        rabbitmqProperties.setAddresses(loadAddresses(rabbitmqProperties, springRabbitmqProperties));
        rabbitmqProperties.setUsername(loadUsername(rabbitmqProperties, springRabbitmqProperties));
        rabbitmqProperties.setPassword(loadPassword(rabbitmqProperties, springRabbitmqProperties));
        rabbitmqProperties.setVirtualHost(loadVirtualHost(rabbitmqProperties, springRabbitmqProperties));
        rabbitmqProperties.setReceiveTimeout(loadReceiveTimeout(rabbitmqProperties, springRabbitmqProperties));
        rabbitmqProperties.setReplyTimeout(loadReplyTimeout(rabbitmqProperties, springRabbitmqProperties));
        rabbitmqProperties.setConcurrentConsumers(loadConcurrentConsumers(rabbitmqProperties, springRabbitmqProperties));
        rabbitmqProperties.setMaxConcurrentConsumers(loadMaxConcurrentConsumers(rabbitmqProperties, springRabbitmqProperties));
    }

    private static Integer loadMaxConcurrentConsumers(RabbitmqProperties rabbitmqProperties, RabbitProperties springRabbitmqProperties) {
        if (rabbitmqProperties.getMaxConcurrentConsumers() == null) {
            if (springRabbitmqProperties.getListener().getSimple().getMaxConcurrency() != null) {
                return springRabbitmqProperties.getListener().getSimple().getMaxConcurrency();
            }
        } else {
            return rabbitmqProperties.getMaxConcurrentConsumers();
        }
        return 1;
    }

    private static Integer loadConcurrentConsumers(RabbitmqProperties rabbitmqProperties, RabbitProperties springRabbitmqProperties) {
        if (rabbitmqProperties.getConcurrentConsumers() == null) {
            if (springRabbitmqProperties.getListener().getSimple().getConcurrency() != null) {
                return springRabbitmqProperties.getListener().getSimple().getConcurrency();
            }
        } else {
            return rabbitmqProperties.getConcurrentConsumers();
        }
        return 1;
    }

    private static Long loadReplyTimeout(RabbitmqProperties rabbitmqProperties, RabbitProperties springRabbitmqProperties) {
        if (rabbitmqProperties.getReplyTimeout() == null) {
            if (springRabbitmqProperties.getTemplate().getReplyTimeout() != null) {
                return springRabbitmqProperties.getTemplate().getReplyTimeout().toMillis();
            }
        } else {
            return rabbitmqProperties.getReplyTimeout();
        }
        return 5000L;
    }

    private static Long loadReceiveTimeout(RabbitmqProperties rabbitmqProperties, RabbitProperties springRabbitmqProperties) {
        if (rabbitmqProperties.getReceiveTimeout() == null) {
            if (springRabbitmqProperties.getTemplate().getReceiveTimeout() != null) {
                return springRabbitmqProperties.getTemplate().getReceiveTimeout().toMillis();
            }
        } else {
            return rabbitmqProperties.getReceiveTimeout();
        }
        return 5000L;
    }

    private static String loadVirtualHost(RabbitmqProperties rabbitmqProperties, RabbitProperties springRabbitmqProperties) {
        if (StringUtils.isBlank(rabbitmqProperties.getVirtualHost())) {
            if (StringUtils.isNotBlank(springRabbitmqProperties.getVirtualHost())) {
                return springRabbitmqProperties.getVirtualHost();
            }
        } else {
            return rabbitmqProperties.getVirtualHost();
        }
        return "/";
    }

    private static String loadPassword(RabbitmqProperties rabbitmqProperties, RabbitProperties springRabbitmqProperties) {
        if (StringUtils.isBlank(rabbitmqProperties.getPassword())) {
            if (StringUtils.isNotBlank(springRabbitmqProperties.getPassword())) {
                return springRabbitmqProperties.getPassword();
            }
        } else {
            return CryptUtils.decrypt(rabbitmqProperties.getPassword(), rabbitmqProperties.getKey());
        }
        return "guest";
    }

    private static String loadUsername(RabbitmqProperties rabbitmqProperties, RabbitProperties springRabbitmqProperties) {
        if (StringUtils.isBlank(rabbitmqProperties.getUsername())) {
            if (StringUtils.isNotBlank(springRabbitmqProperties.getUsername())) {
                return springRabbitmqProperties.getUsername();
            }
        } else {
            return rabbitmqProperties.getUsername();
        }
        return "guest";
    }

    private static String loadAddresses(RabbitmqProperties rabbitmqProperties, RabbitProperties springRabbitmqProperties) {
        if (StringUtils.isBlank(rabbitmqProperties.getAddresses())) {
            if (StringUtils.isNotBlank(springRabbitmqProperties.getAddresses())) {
                return springRabbitmqProperties.getAddresses();
            } else if (StringUtils.isNotBlank(springRabbitmqProperties.getHost())) {
                if (springRabbitmqProperties.getPort() != null) {
                    return springRabbitmqProperties.getHost() + ":" + springRabbitmqProperties.getPort();
                } else {
                    return springRabbitmqProperties.getHost();
                }
            }
        } else {
            return rabbitmqProperties.getAddresses();
        }
        return "127.0.0.1:5672";
    }

}
