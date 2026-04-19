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
package wang.bigbird.domain.framework.data.rocketmq.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * rocketmq生产者属性
 *
 * @author Bigbird
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "bigbird.data.rocketmq.producer")
public class RocketmqProducerProperties {

    /**
     * 加解密密钥
     */
    private String key = "bigbird";
    /**
     * 节点地址，rocketmq集群地址中的某一个NameServer地址
     */
    private String address;
    /**
     * 通道访问方式，可用值：LOCAL、CLOUD
     */
    private String accessChannel;
    /**
     * 生产者组名
     */
    private String group;
    /**
     * 安全认证相关的key
     */
    private String accessKey;
    /**
     * 安全认证相关的secret
     */
    private String secretKey;
    /**
     * 是否打开消息轨迹，默认是false
     */
    private Boolean enableMsgTrace;
    /**
     * 配置将消息轨迹数据存储到用户指定的Topic
     */
    private String customizedTraceTopic;
    /**
     * 发送消息的超时时间，单位毫秒
     */
    private Integer sendMessageTimeout;
    /**
     * 消息超过设置的字节大小就开始压缩
     */
    private Integer compressMessageBodyThreshold;
    /**
     * 同步发送消息失败的重试次数
     */
    private Integer retryTimesWhenSendFailed;
    /**
     * 异步发送消息失败的重试次数
     */
    private Integer retryTimesWhenSendAsyncFailed;
    /**
     * 开启内部消息重试
     */
    private Boolean retryNextServer;
    /**
     * 限制消息的大小，单位字节，默认4M
     */
    private Integer maxMessageSize;

}
