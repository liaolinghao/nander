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

import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.support.RocketMQUtil;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.data.rocketmq.config.property.RocketmqProducerProperties;

/**
 * 消息生产者创建器
 *
 * @author Bigbird
 */
public class MQProducerHelper {

    /**
     * 生产者实例名称前缀
     */
    private final static String PRODUCER_NAME_PREFIX = "BIGBIRD-PRODUCER-INSTANCE-";

    /**
     * 创建消息生产者
     *
     * @param producerGroupName          生产者组名，组的意义在于集群模式下，当一个节点挂掉，可以依靠组内其他节点继续工作。
     * @param rocketmqProducerProperties 生产者配置属性
     * @return 消息生产者
     */
    public static DefaultMQProducer createDefaultMQProducer(String producerGroupName, RocketmqProducerProperties rocketmqProducerProperties) {
        String accessChannel = rocketmqProducerProperties.getAccessChannel();
        String ak = rocketmqProducerProperties.getAccessKey();
        String sk = rocketmqProducerProperties.getSecretKey();
        boolean isEnableMsgTrace = rocketmqProducerProperties.getEnableMsgTrace();
        String customizedTraceTopic = rocketmqProducerProperties.getCustomizedTraceTopic();
        DefaultMQProducer producer = RocketMQUtil.createDefaultMQProducer(producerGroupName, ak, sk, isEnableMsgTrace, customizedTraceTopic);
        producer.setNamesrvAddr(rocketmqProducerProperties.getAddress());
        if (!org.springframework.util.StringUtils.isEmpty(accessChannel)) {
            producer.setAccessChannel(AccessChannel.valueOf(accessChannel));
        }
        producer.setSendMsgTimeout(rocketmqProducerProperties.getSendMessageTimeout());
        producer.setRetryTimesWhenSendFailed(rocketmqProducerProperties.getRetryTimesWhenSendFailed());
        producer.setRetryTimesWhenSendAsyncFailed(rocketmqProducerProperties.getRetryTimesWhenSendAsyncFailed());
        producer.setMaxMessageSize(rocketmqProducerProperties.getMaxMessageSize());
        producer.setCompressMsgBodyOverHowmuch(rocketmqProducerProperties.getCompressMessageBodyThreshold());
        producer.setRetryAnotherBrokerWhenNotStoreOK(rocketmqProducerProperties.getRetryNextServer());
        // 必须设置唯一实例名称，防止采用默认名称，导致对同一个producer重复调用start方法，抛出The producer
        // group[xxx] has been created before, specify another name please.
        producer.setInstanceName(StringUtils.joinStr(PRODUCER_NAME_PREFIX,
                StringUtils.getUuid()));
        return producer;
    }

}
