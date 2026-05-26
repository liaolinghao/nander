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
package wang.bigbird.domain.framework.server.web.ban.support.repository;

import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.common.forbidden.support.core.Dfa;
import wang.bigbird.domain.framework.common.forbidden.support.repository.AbstractForbidWordRepository;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.data.redis.service.base.IRedisSetService;

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

    private static final String FORBID_WORD_KEY = "word:forbid";

    private final IRedisSetService redisSetService;

    /**
     * 传入一个空的DFA实例
     *
     * @param dfa DFA实例
     */
    public RedisForbidWordRepository(Dfa dfa, IRedisSetService redisSetService) {
        super(dfa);
        this.redisSetService = redisSetService;
        refresh(false);
    }

    @Override
    protected Iterator<String> loadForbidWords() {
        Set<String> words = redisSetService.smembers(FORBID_WORD_KEY, String.class);
        if (CollectionUtils.isEmpty(words)) {
            log.warn("No forbid words found.");
            return Collections.emptyIterator();
        }
        log.info("Loaded {} forbid words", words.size());
        return words.iterator();
    }

}
