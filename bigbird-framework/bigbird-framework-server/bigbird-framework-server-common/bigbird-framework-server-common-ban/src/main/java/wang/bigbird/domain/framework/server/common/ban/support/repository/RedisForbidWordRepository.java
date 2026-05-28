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
package wang.bigbird.domain.framework.server.common.ban.support.repository;

import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.common.forbidden.support.core.Dfa;
import wang.bigbird.domain.framework.common.forbidden.support.repository.AbstractForbidWordRepository;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.data.redis.service.base.IRedisPubSubService;
import wang.bigbird.domain.framework.data.redis.service.base.IRedisSetService;
import wang.bigbird.domain.framework.server.common.ban.base.enums.RefreshTypeEnum;
import wang.bigbird.domain.framework.server.common.ban.domain.pojo.msg.ForbidWordRefreshEvent;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * 从Redis加载禁用词
 *
 * @author Bigbird
 */
@Slf4j
public class RedisForbidWordRepository extends AbstractForbidWordRepository {

    private String forbidWordPoolKey;
    private String forbidWordRefreshEventTopic;

    private IRedisSetService redisSetService;
    private IRedisPubSubService redisPubSubService;

    /**
     * 传入一个空的DFA实例
     *
     * @param dfa DFA实例
     */
    public RedisForbidWordRepository(Dfa dfa, String forbidWordPoolKey, String forbidWordRefreshEventTopic, IRedisSetService redisSetService, IRedisPubSubService redisPubSubService) {
        super(dfa);
        this.forbidWordPoolKey = forbidWordPoolKey;
        this.forbidWordRefreshEventTopic = forbidWordRefreshEventTopic;
        this.redisSetService = redisSetService;
        this.redisPubSubService = redisPubSubService;
        refresh(false);
        redisPubSubService.subscribe(forbidWordRefreshEventTopic, (pattern, channel, msg) -> {
            ForbidWordRefreshEvent forbidWordRefreshEvent = JsonUtils.json2Object(msg, ForbidWordRefreshEvent.class);
            switch (forbidWordRefreshEvent.getRefreshType()) {
                case ADD:
                    dfa.addWord(forbidWordRefreshEvent.getWords().iterator());
                    break;
                case DELETE:
                    dfa.removeWord(forbidWordRefreshEvent.getWords().iterator());
                    break;
                case REFRESH:
                    refresh(true);
                    break;
                default:
                    break;
            }
        });
    }

    @Override
    protected Iterator<String> loadForbidWords() {
        Set<String> words = redisSetService.smembers(forbidWordPoolKey, String.class);
        if (CollectionUtils.isEmpty(words)) {
            log.warn("No forbid words found.");
            return Collections.emptyIterator();
        }
        log.info("Loaded {} forbid words", words.size());
        return words.iterator();
    }

}
