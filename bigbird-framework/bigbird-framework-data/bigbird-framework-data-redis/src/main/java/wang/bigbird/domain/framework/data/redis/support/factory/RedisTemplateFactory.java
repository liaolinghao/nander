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
package wang.bigbird.domain.framework.data.redis.support.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * redisTemplate 工厂
 * 用于生成不同类型的 redisTemplate
 *
 * @author Bigbird
 */
public class RedisTemplateFactory {

    private static final Map<Class, RedisTemplate> TEMPLATE_MAP = new ConcurrentHashMap<>();

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisTemplateFactory(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    /**
     * 创建 RedisTemplate
     */
    public synchronized <T> RedisTemplate<String, T> create(Class<T> clazz) {
        return TEMPLATE_MAP.computeIfAbsent(clazz, o -> {
            RedisTemplate<String, T> template = new RedisTemplate<>();
            template.setConnectionFactory(redisConnectionFactory);
            template.setKeySerializer(new StringRedisSerializer());
            template.setHashKeySerializer(new StringRedisSerializer());
            Jackson2JsonRedisSerializer<T> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(clazz);
            ObjectMapper mapper = JsonUtils.getMapper();
            jackson2JsonRedisSerializer.setObjectMapper(mapper);
            template.setValueSerializer(jackson2JsonRedisSerializer);
            template.setHashValueSerializer(jackson2JsonRedisSerializer);
            return template;
        });
    }


}
