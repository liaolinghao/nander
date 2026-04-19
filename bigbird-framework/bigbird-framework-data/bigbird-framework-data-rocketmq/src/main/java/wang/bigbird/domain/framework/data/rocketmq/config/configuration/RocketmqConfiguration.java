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
package wang.bigbird.domain.framework.data.rocketmq.config.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import wang.bigbird.domain.framework.data.rocketmq.base.helper.MQProducerHelper;
import wang.bigbird.domain.framework.data.rocketmq.base.helper.PropertiesHelper;
import wang.bigbird.domain.framework.data.rocketmq.config.property.RocketmqConsumerProperties;
import wang.bigbird.domain.framework.data.rocketmq.config.property.RocketmqProducerProperties;

import javax.annotation.PostConstruct;

/**
 * Rocketmq 配置
 *
 * @author Bigbird
 */
@Configuration
@Slf4j
@ComponentScan(basePackages = "wang.bigbird.domain.framework.data.rocketmq")
public class RocketmqConfiguration {

    @PostConstruct
    public void init() {
        log.info("Init rocketmq framework.");
    }

    @Bean
    @Primary
    public DefaultMQProducer defaultMQProducer(RocketmqProducerProperties rocketmqProducerProperties, RocketmqConsumerProperties rocketmqConsumerProperties, org.apache.rocketmq.spring.autoconfigure.RocketMQProperties springRocketmqProperties) {
        PropertiesHelper.combineRocketmqProperties(rocketmqProducerProperties, rocketmqConsumerProperties, springRocketmqProperties);
        return buildDefaultMQProducer(rocketmqProducerProperties);
    }

    private DefaultMQProducer buildDefaultMQProducer(RocketmqProducerProperties rocketmqProducerProperties) {
        return MQProducerHelper.createDefaultMQProducer(rocketmqProducerProperties.getGroup(), rocketmqProducerProperties);
    }


}
