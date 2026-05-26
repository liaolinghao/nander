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
package wang.bigbird.domain.framework.data.redis.service.base;

import wang.bigbird.domain.framework.data.redis.support.handler.RedisSubscriberHandler;

/**
 * redis 发布订阅服务
 *
 * @author Bigbird
 */
public interface IRedisPubSubService {

    /**
     * 发布消息
     *
     * @param channel 渠道
     * @param message 消息
     */
    void publish(String channel, String message);

    /**
     * 渠道消息订阅
     *
     * @param channel                渠道
     * @param redisSubscriberHandler 消息订阅处理器
     */
    void subscribe(String channel, RedisSubscriberHandler redisSubscriberHandler);

    /**
     * 可靠渠道消息订阅
     *
     * @param channel                渠道
     * @param redisSubscriberHandler 消息订阅处理器
     */
    void reliableSubscribe(String channel, RedisSubscriberHandler redisSubscriberHandler);

    /**
     * 多渠道消息订阅
     *
     * @param channelPattern         渠道模式，用 glob 风格通配符（*、?、[]）匹配多个 channel
     * @param redisSubscriberHandler 消息订阅处理器
     */
    void patternSubscribe(String channelPattern, RedisSubscriberHandler redisSubscriberHandler);

}
