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

import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.data.redis.service.base.IRedisSetService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * redis set 服务
 *
 * @author Bigbird
 */
@Service
public class RedisSetServiceImpl implements IRedisSetService {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public boolean sadd(String key, Object value) {
        RSet<String> set = redissonClient.getSet(key);
        String jsonString = JsonUtils.object2Json(value);
        return set.add(jsonString);
    }

    @Override
    public boolean sadd(String key, Set<?> objs) {
        RSet<String> set = redissonClient.getSet(key);
        Set<String> jsonList = objs.stream()
                .map(JsonUtils::object2Json)
                .collect(Collectors.toSet());
        return set.addAll(jsonList);
    }

    @Override
    public boolean sismember(String key, Object value) {
        RSet<String> set = redissonClient.getSet(key);
        String jsonString = JsonUtils.object2Json(value);
        return set.contains(jsonString);
    }

    @Override
    public <T> T spop(String key, Class<T> clazz) {
        RSet<String> set = redissonClient.getSet(key);
        String jsonString = set.removeRandom();
        return JsonUtils.json2Object(jsonString, clazz);
    }

    @Override
    public boolean srem(String key, Object value) {
        RSet<String> set = redissonClient.getSet(key);
        String jsonString = JsonUtils.object2Json(value);
        return set.remove(jsonString);
    }

    @Override
    public boolean smove(String key, String destKey, Object value) {
        RSet<String> set = redissonClient.getSet(key);
        String jsonString = JsonUtils.object2Json(value);
        return set.move(destKey, jsonString);
    }

    @Override
    public int scard(String key) {
        RSet<String> set = redissonClient.getSet(key);
        return set.size();
    }

    @Override
    public <T> Set<T> smembers(String key, Class<T> clazz) {
        RSet<String> set = redissonClient.getSet(key);
        Set<String> jsonStrings = set.readAll();
        return convertJsonToObjects(jsonStrings, clazz);
    }

    @Override
    public <T> Set<T> sinter(List<String> keys, Class<T> clazz) {
        if (CollectionUtils.isEmpty(keys)) {
            return new HashSet<>(2);
        }
        String firstKey = keys.get(0);
        if (keys.size() == 1) {
            return smembers(firstKey, clazz);
        }
        List<String> remainedKey = keys.subList(1, keys.size());
        RSet<String> set = redissonClient.getSet(firstKey);
        Set<String> jsonStrings = set.readIntersection(remainedKey.toArray(new String[]{}));
        return convertJsonToObjects(jsonStrings, clazz);
    }

    @Override
    public <T> Set<T> sunion(List<String> keys, Class<T> clazz) {
        if (CollectionUtils.isEmpty(keys)) {
            return new HashSet<>(2);
        }
        String firstKey = keys.get(0);
        if (keys.size() == 1) {
            return smembers(firstKey, clazz);
        }
        List<String> remainedKey = keys.subList(1, keys.size());
        RSet<String> set = redissonClient.getSet(firstKey);
        Set<String> jsonStrings = set.readUnion(remainedKey.toArray(new String[]{}));
        return convertJsonToObjects(jsonStrings, clazz);
    }

    @Override
    public <T> Set<T> sdiff(List<String> keys, Class<T> clazz) {
        if (CollectionUtils.isEmpty(keys)) {
            return new HashSet<>(2);
        }
        String firstKey = keys.get(0);
        if (keys.size() == 1) {
            return smembers(firstKey, clazz);
        }
        List<String> remainedKey = keys.subList(1, keys.size());
        RSet<String> set = redissonClient.getSet(firstKey);
        Set<String> jsonStrings = set.readDiff(remainedKey.toArray(new String[]{}));
        return convertJsonToObjects(jsonStrings, clazz);
    }

    /**
     * json转对象
     *
     * @param jsonStrings json字符串列表
     * @param clazz       类型
     * @return 对象集合
     */
    private <T> Set<T> convertJsonToObjects(Set<String> jsonStrings, Class<T> clazz) {
        if (CollectionUtils.isEmpty(jsonStrings)) {
            return new HashSet<>(0);
        }
        return jsonStrings.stream()
                .map(jsonString -> JsonUtils.json2Object(jsonString, clazz))
                .collect(Collectors.toSet());
    }

}
