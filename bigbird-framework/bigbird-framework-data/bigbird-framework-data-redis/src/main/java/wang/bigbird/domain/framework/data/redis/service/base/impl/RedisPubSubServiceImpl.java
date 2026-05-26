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
package wang.bigbird.domain.framework.data.redis.service.base.impl;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RPatternTopic;
import org.redisson.api.RReliableTopic;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.data.redis.service.base.IRedisPubSubService;
import wang.bigbird.domain.framework.data.redis.support.handler.RedisSubscriberHandler;

/**
 * redis 发布订阅服务
 *
 * @author Bigbird
 */
@Slf4j
@Service
public class RedisPubSubServiceImpl implements IRedisPubSubService {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void publish(String channel, String message) {
        RTopic topic = redissonClient.getTopic(channel);
        topic.publish(message);
    }

    @Override
    public void subscribe(String channel, RedisSubscriberHandler redisSubscriberHandler) {
        RTopic topic = redissonClient.getTopic(channel);
        topic.addListener(String.class, (c, msg) -> {
            log.debug("Channel:{},Msg:{}.", c, msg);
            redisSubscriberHandler.handle(c, c, msg);
        });
    }

    @Override
    public void reliableSubscribe(String channel, RedisSubscriberHandler redisSubscriberHandler) {
        RReliableTopic reliableTopic = redissonClient.getReliableTopic(channel);
        reliableTopic.addListener(String.class, (c, msg) -> {
            log.debug("Channel:{},Msg:{}.", c, msg);
            redisSubscriberHandler.handle(c, c, msg);
        });
    }

    @Override
    public void patternSubscribe(String channelPattern, RedisSubscriberHandler redisSubscriberHandler) {
        RPatternTopic patternTopic = redissonClient.getPatternTopic(channelPattern);
        patternTopic.addListener(String.class, (pattern, channel, msg) -> {
            log.debug("Pattern:{},Channel:{},Msg:{}.", pattern, channel, msg);
            redisSubscriberHandler.handle(pattern, channel, msg);
        });
    }

}
