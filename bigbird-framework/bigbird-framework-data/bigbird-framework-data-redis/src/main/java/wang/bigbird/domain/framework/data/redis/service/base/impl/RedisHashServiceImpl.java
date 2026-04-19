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

import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.data.redis.service.base.IRedisHashService;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * redis hash 服务
 *
 * @author Bigbird
 */
@Service
public class RedisHashServiceImpl implements IRedisHashService {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public int hset(String key, String field, Object value) {
        RMap<String, String> map = redissonClient.getMap(key);
        String jsonString = JsonUtils.object2Json(value);
        Object ret = map.put(field, jsonString);
        if (null == ret) {
            return 1;
        }
        return 0;
    }

    @Override
    public boolean hsetnx(String key, String field, Object value) {
        RMap<String, String> map = redissonClient.getMap(key);
        String jsonString = JsonUtils.object2Json(value);
        return map.fastPutIfAbsent(field, jsonString);
    }

    @Override
    public <T> T hget(String key, String field, Class<T> clazz) {
        RMap<String, String> map = redissonClient.getMap(key);
        String jsonString = map.get(field);
        return JsonUtils.json2Object(jsonString, clazz);
    }

    @Override
    public boolean hexists(String key, String field) {
        RMap<String, String> map = redissonClient.getMap(key);
        return map.containsKey(field);
    }

    @Override
    public int hdel(String key, String... fields) {
        RMap<String, String> map = redissonClient.getMap(key);
        return (int) map.fastRemove(fields);
    }

    @Override
    public int hlen(String key) {
        RMap<String, String> map = redissonClient.getMap(key);
        return map.size();
    }

    @Override
    public Number hincrby(String key, String field, Number delta) {
        RMap<String, Number> map = redissonClient.getMap(key);
        return map.addAndGet(field, delta);
    }

    @Override
    public void hmset(String key, Map<String, Object> fieldValueMap) {
        RMap<String, String> map = redissonClient.getMap(key);
        Assert.isFalse(null == fieldValueMap || fieldValueMap.isEmpty(), "fieldValueMap must not null or empty.");
        Map<String, String> newFieldValueMap = fieldValueMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        o -> JsonUtils.object2Json(o.getValue())));
        map.putAll(newFieldValueMap);
    }

    @Override
    public <T> Map<String, T> hmget(String key, Collection<String> fields, Class<T> clazz) {
        RMap<String, String> map = redissonClient.getMap(key);
        if (CollectionUtils.isEmpty(fields)) {
            return null;
        }
        Map<String, String> retMap = map.getAll(new HashSet<>(fields));
        if (CollectionUtils.isEmpty(retMap)) {
            return null;
        }
        return retMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        o -> JsonUtils.json2Object(o.getValue(), clazz)));
    }

    @Override
    public <T> Map<String, T> hgetall(String key, Class<T> clazz) {
        RMap<String, String> map = redissonClient.getMap(key);
        Set<Map.Entry<String, String>> entries = map.readAllEntrySet();
        if (CollectionUtils.isEmpty(entries)) {
            return null;
        }
        return entries.stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        o -> JsonUtils.json2Object(o.getValue(), clazz)));
    }

    @Override
    public Set<String> hkeys(String key) {
        RMap<String, String> map = redissonClient.getMap(key);
        return map.readAllKeySet();
    }
}
