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
package wang.bigbird.domain.framework.server.web.ban.service.base.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.data.redis.service.base.IRedisPubSubService;
import wang.bigbird.domain.framework.data.redis.service.base.IRedisSetService;
import wang.bigbird.domain.framework.server.web.ban.base.enums.RefreshTypeEnum;
import wang.bigbird.domain.framework.server.web.ban.config.property.BanProperties;
import wang.bigbird.domain.framework.server.web.ban.domain.pojo.msg.ForbidWordRefreshEvent;
import wang.bigbird.domain.framework.server.web.ban.service.base.IForbidWordChangeService;

import java.util.Set;

/**
 * 禁用词变更服务
 *
 * @author Bigbird
 */
@Slf4j
@Service
public class ForbidWordChangeServiceImpl implements IForbidWordChangeService {

    @Autowired
    private BanProperties banProperties;

    @Autowired
    private IRedisPubSubService redisPubSubService;
    @Autowired
    private IRedisSetService redisSetService;

    @Override
    public void add(Set<String> words) {
        ForbidWordRefreshEvent forbidWordRefreshEvent = new ForbidWordRefreshEvent();
        forbidWordRefreshEvent.setRefreshType(RefreshTypeEnum.ADD);
        forbidWordRefreshEvent.setWords(words);
        redisPubSubService.publish(banProperties.getForbidWordRefreshEventTopic(), JsonUtils.object2Json(forbidWordRefreshEvent));
    }

    @Override
    public void remove(Set<String> words) {
        ForbidWordRefreshEvent forbidWordRefreshEvent = new ForbidWordRefreshEvent();
        forbidWordRefreshEvent.setRefreshType(RefreshTypeEnum.DELETE);
        forbidWordRefreshEvent.setWords(words);
        redisPubSubService.publish(banProperties.getForbidWordRefreshEventTopic(), JsonUtils.object2Json(forbidWordRefreshEvent));
    }

    @Override
    public void refresh() {
        ForbidWordRefreshEvent forbidWordRefreshEvent = new ForbidWordRefreshEvent();
        forbidWordRefreshEvent.setRefreshType(RefreshTypeEnum.REFRESH);
        redisPubSubService.publish(banProperties.getForbidWordRefreshEventTopic(), JsonUtils.object2Json(forbidWordRefreshEvent));
    }

    @Override
    public void initPool(Set<String> words) {
        redisSetService.sadd(banProperties.getForbidWordPoolKey(), words);
    }

}
