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
package wang.bigbird.domain.framework.server.web.ban.config.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import wang.bigbird.domain.framework.common.forbidden.service.base.IForbidWordService;
import wang.bigbird.domain.framework.common.forbidden.service.base.impl.ForbidWordServiceImpl;
import wang.bigbird.domain.framework.common.forbidden.support.core.Dfa;
import wang.bigbird.domain.framework.common.forbidden.support.core.MemoryMapDfaImpl;
import wang.bigbird.domain.framework.data.redis.service.base.IRedisSetService;
import wang.bigbird.domain.framework.server.web.ban.support.repository.RedisForbidWordRepository;

import javax.annotation.PostConstruct;

/**
 * WEB框架配置
 *
 * @author Bigbird
 */
@Slf4j
@ComponentScan("wang.bigbird.domain.framework.server.web.ban")
@Configuration
public class WebBanConfiguration {

    @PostConstruct
    public void init() {
        log.info("Init ban web framework.");
    }

    @Bean
    public Dfa dfa() {
        return new MemoryMapDfaImpl();
    }

    @Bean
    public RedisForbidWordRepository sensitiveWordRepository(Dfa dfa, IRedisSetService redisSetService) {
        return new RedisForbidWordRepository(dfa, redisSetService);
    }

    @Bean
    public IForbidWordService forbidWordService(RedisForbidWordRepository redisForbidWordRepository) {
        return new ForbidWordServiceImpl(redisForbidWordRepository);
    }

}
