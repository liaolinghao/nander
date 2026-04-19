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
package wang.bigbird.domain.framework.data.rocketmq.base.helper;

import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import wang.bigbird.domain.framework.core.base.util.CryptUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.data.rocketmq.config.property.RocketmqConsumerProperties;
import wang.bigbird.domain.framework.data.rocketmq.config.property.RocketmqProducerProperties;

/**
 * 属性设置器
 *
 * @author Bigbird
 */
public class PropertiesHelper {

    /**
     * 合并rocketmq配置
     * <p>
     * 配置加载优先级如下：
     * <p>
     * 1、自定义配置优先
     * <p>
     * 2、spring原生配置作为候补
     *
     * @param rocketmqProducerProperties 自定义rocketmq生产者属性
     * @param rocketmqConsumerProperties 自定义rocketmq消费者属性
     * @param springRocketmqProperties   spring原生的rocketmq配置
     */
    public static void combineRocketmqProperties(RocketmqProducerProperties rocketmqProducerProperties, RocketmqConsumerProperties rocketmqConsumerProperties, RocketMQProperties springRocketmqProperties) {
        rocketmqProducerProperties.setAddress(loadAddress(rocketmqProducerProperties, springRocketmqProperties));
        rocketmqProducerProperties.setAccessChannel(loadAccessChannel(rocketmqProducerProperties, springRocketmqProperties));
        rocketmqProducerProperties.setGroup(loadGroup(rocketmqProducerProperties, springRocketmqProperties));
        rocketmqProducerProperties.setAccessKey(loadAccessKey(rocketmqProducerProperties, springRocketmqProperties));
        rocketmqProducerProperties.setSecretKey(loadSecretKey(rocketmqProducerProperties, springRocketmqProperties));
        rocketmqProducerProperties.setEnableMsgTrace(loadEnableMsgTrace(rocketmqProducerProperties, springRocketmqProperties));
        rocketmqProducerProperties.setCustomizedTraceTopic(loadCustomizedTraceTopic(rocketmqProducerProperties, springRocketmqProperties));
        rocketmqProducerProperties.setSendMessageTimeout(loadSendMessageTimeout(rocketmqProducerProperties, springRocketmqProperties));
        rocketmqProducerProperties.setCompressMessageBodyThreshold(loadCompressMessageBodyThreshold(rocketmqProducerProperties, springRocketmqProperties));
        rocketmqProducerProperties.setRetryTimesWhenSendFailed(loadRetryTimesWhenSendFailed(rocketmqProducerProperties, springRocketmqProperties));
        rocketmqProducerProperties.setRetryTimesWhenSendAsyncFailed(loadRetryTimesWhenSendAsyncFailed(rocketmqProducerProperties, springRocketmqProperties));
        rocketmqProducerProperties.setRetryNextServer(loadRetryNextServer(rocketmqProducerProperties, springRocketmqProperties));
        rocketmqProducerProperties.setMaxMessageSize(loadMaxMessageSize(rocketmqProducerProperties, springRocketmqProperties));
        rocketmqConsumerProperties.setAddress(loadAddress(rocketmqConsumerProperties, springRocketmqProperties));
    }

    private static String loadAddress(RocketmqConsumerProperties rocketmqConsumerProperties, RocketMQProperties springRocketmqProperties) {
        if (StringUtils.isBlank(rocketmqConsumerProperties.getAddress())) {
            if (StringUtils.isNotBlank(springRocketmqProperties.getNameServer())) {
                return springRocketmqProperties.getNameServer();
            }
        } else {
            return rocketmqConsumerProperties.getAddress();
        }
        return "127.0.0.1:9876";
    }

    private static Integer loadMaxMessageSize(RocketmqProducerProperties rocketmqProducerProperties, RocketMQProperties springRocketmqProperties) {
        if (rocketmqProducerProperties.getMaxMessageSize() == null) {
            RocketMQProperties.Producer producer = springRocketmqProperties.getProducer();
            if (producer != null) {
                return producer.getMaxMessageSize();
            }
        } else {
            return rocketmqProducerProperties.getMaxMessageSize();
        }
        return 4194304;
    }

    private static Boolean loadRetryNextServer(RocketmqProducerProperties rocketmqProducerProperties, RocketMQProperties springRocketmqProperties) {
        if (rocketmqProducerProperties.getRetryNextServer() == null) {
            RocketMQProperties.Producer producer = springRocketmqProperties.getProducer();
            if (producer != null) {
                return producer.isRetryNextServer();
            }
        } else {
            return rocketmqProducerProperties.getRetryNextServer();
        }
        return false;
    }

    private static Integer loadRetryTimesWhenSendAsyncFailed(RocketmqProducerProperties rocketmqProducerProperties, RocketMQProperties springRocketmqProperties) {
        if (rocketmqProducerProperties.getRetryTimesWhenSendAsyncFailed() == null) {
            RocketMQProperties.Producer producer = springRocketmqProperties.getProducer();
            if (producer != null) {
                return producer.getRetryTimesWhenSendAsyncFailed();
            }
        } else {
            return rocketmqProducerProperties.getRetryTimesWhenSendAsyncFailed();
        }
        return 2;
    }

    private static Integer loadRetryTimesWhenSendFailed(RocketmqProducerProperties rocketmqProducerProperties, RocketMQProperties springRocketmqProperties) {
        if (rocketmqProducerProperties.getRetryTimesWhenSendFailed() == null) {
            RocketMQProperties.Producer producer = springRocketmqProperties.getProducer();
            if (producer != null) {
                return producer.getRetryTimesWhenSendFailed();
            }
        } else {
            return rocketmqProducerProperties.getRetryTimesWhenSendFailed();
        }
        return 2;
    }

    private static Integer loadCompressMessageBodyThreshold(RocketmqProducerProperties rocketmqProducerProperties, RocketMQProperties springRocketmqProperties) {
        if (rocketmqProducerProperties.getCompressMessageBodyThreshold() == null) {
            RocketMQProperties.Producer producer = springRocketmqProperties.getProducer();
            if (producer != null) {
                return producer.getCompressMessageBodyThreshold();
            }
        } else {
            return rocketmqProducerProperties.getCompressMessageBodyThreshold();
        }
        return 4096;
    }

    private static Integer loadSendMessageTimeout(RocketmqProducerProperties rocketmqProducerProperties, RocketMQProperties springRocketmqProperties) {
        if (rocketmqProducerProperties.getSendMessageTimeout() == null) {
            RocketMQProperties.Producer producer = springRocketmqProperties.getProducer();
            if (producer != null) {
                return producer.getSendMessageTimeout();
            }
        } else {
            return rocketmqProducerProperties.getSendMessageTimeout();
        }
        return 3000;
    }

    private static String loadCustomizedTraceTopic(RocketmqProducerProperties rocketmqProducerProperties, RocketMQProperties springRocketmqProperties) {
        if (StringUtils.isBlank(rocketmqProducerProperties.getCustomizedTraceTopic())) {
            RocketMQProperties.Producer producer = springRocketmqProperties.getProducer();
            if (producer != null) {
                if (StringUtils.isNotBlank(producer.getCustomizedTraceTopic())) {
                    return producer.getCustomizedTraceTopic();
                }
            }
        } else {
            return rocketmqProducerProperties.getCustomizedTraceTopic();
        }
        return "RMQ_SYS_TRACE_TOPIC";
    }


    private static Boolean loadEnableMsgTrace(RocketmqProducerProperties rocketmqProducerProperties, RocketMQProperties springRocketmqProperties) {
        if (rocketmqProducerProperties.getEnableMsgTrace() == null) {
            RocketMQProperties.Producer producer = springRocketmqProperties.getProducer();
            if (producer != null) {
                return producer.isEnableMsgTrace();
            }
        } else {
            return rocketmqProducerProperties.getEnableMsgTrace();
        }
        return false;
    }

    private static String loadSecretKey(RocketmqProducerProperties rocketmqProducerProperties, RocketMQProperties springRocketmqProperties) {
        if (StringUtils.isBlank(rocketmqProducerProperties.getSecretKey())) {
            RocketMQProperties.Producer producer = springRocketmqProperties.getProducer();
            if (producer != null) {
                if (StringUtils.isNotBlank(producer.getSecretKey())) {
                    return producer.getSecretKey();
                }
            }
        } else {
            return CryptUtils.decrypt(rocketmqProducerProperties.getSecretKey(), rocketmqProducerProperties.getKey());
        }
        return null;
    }

    private static String loadAccessKey(RocketmqProducerProperties rocketmqProducerProperties, RocketMQProperties springRocketmqProperties) {
        if (StringUtils.isBlank(rocketmqProducerProperties.getAccessKey())) {
            RocketMQProperties.Producer producer = springRocketmqProperties.getProducer();
            if (producer != null) {
                if (StringUtils.isNotBlank(producer.getAccessKey())) {
                    return producer.getAccessKey();
                }
            }
        } else {
            return CryptUtils.decrypt(rocketmqProducerProperties.getAccessKey(), rocketmqProducerProperties.getKey());
        }
        return null;
    }

    private static String loadGroup(RocketmqProducerProperties rocketmqProducerProperties, RocketMQProperties springRocketmqProperties) {
        if (StringUtils.isBlank(rocketmqProducerProperties.getGroup())) {
            RocketMQProperties.Producer producer = springRocketmqProperties.getProducer();
            if (producer != null) {
                if (StringUtils.isNotBlank(producer.getGroup())) {
                    return producer.getGroup();
                }
            }
        } else {
            return rocketmqProducerProperties.getGroup();
        }
        return "bigbird-group";
    }

    private static String loadAccessChannel(RocketmqProducerProperties rocketmqProducerProperties, RocketMQProperties springRocketmqProperties) {
        if (StringUtils.isBlank(rocketmqProducerProperties.getAccessChannel())) {
            if (StringUtils.isNotBlank(springRocketmqProperties.getAccessChannel())) {
                return springRocketmqProperties.getAccessChannel();
            }
        } else {
            return rocketmqProducerProperties.getAccessChannel();
        }
        return "LOCAL";
    }

    private static String loadAddress(RocketmqProducerProperties rocketmqProducerProperties, RocketMQProperties springRocketmqProperties) {
        if (StringUtils.isBlank(rocketmqProducerProperties.getAddress())) {
            if (StringUtils.isNotBlank(springRocketmqProperties.getNameServer())) {
                return springRocketmqProperties.getNameServer();
            }
        } else {
            return rocketmqProducerProperties.getAddress();
        }
        return "127.0.0.1:9876";
    }

}
