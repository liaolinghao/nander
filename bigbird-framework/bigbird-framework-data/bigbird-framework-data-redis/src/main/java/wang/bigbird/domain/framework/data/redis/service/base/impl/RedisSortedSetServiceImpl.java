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
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.data.redis.service.base.IRedisSortedSetService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * redis 有序set 服务
 *
 * @author Bigbird
 */
@Service
public class RedisSortedSetServiceImpl implements IRedisSortedSetService {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public boolean zadd(String key, double score, Object value) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        String jsonString = JsonUtils.object2Json(value);
        return scoredSortedSet.add(score, jsonString);
    }

    @Override
    public Double zscore(String key, Object value) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        String jsonString = JsonUtils.object2Json(value);
        return scoredSortedSet.getScore(jsonString);
    }

    @Override
    public Double zincrby(String key, Object value, Number delta) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        String jsonString = JsonUtils.object2Json(value);
        return scoredSortedSet.addScore(jsonString, delta);
    }

    @Override
    public int zcard(String key) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.size();
    }

    @Override
    public int zcount(String key, double startScore, double endScore) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.count(startScore, true, endScore, true);
    }

    @Override
    public <T> List<T> zrange(String key, int start, int end, Class<T> clazz) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<String> jsonStrings = scoredSortedSet.valueRange(start, end);
        return convertJsonToObjects(jsonStrings, clazz);
    }

    @Override
    public <T> List<T> zrevrange(String key, int start, int end, Class<T> clazz) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<String> jsonStrings = scoredSortedSet.valueRangeReversed(start, end);
        return convertJsonToObjects(jsonStrings, clazz);
    }

    @Override
    public <T> List<T> zrange(String key, double startScore, double endScore, Class<T> clazz) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<String> jsonStrings = scoredSortedSet.valueRange(startScore, true, endScore, true);
        return convertJsonToObjects(jsonStrings, clazz);
    }

    @Override
    public <T> List<T> zrevrange(String key, double startScore, double endScore, Class<T> clazz) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<String> jsonStrings = scoredSortedSet.valueRangeReversed(startScore, true, endScore, true);
        return convertJsonToObjects(jsonStrings, clazz);
    }

    @Override
    public <T> List<T> zrangebypage(String key, double startScore, double endScore, int offset, int count, Class<T> clazz) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<String> jsonStrings = scoredSortedSet.valueRange(startScore, true, endScore, true, offset, count);
        return convertJsonToObjects(jsonStrings, clazz);
    }

    @Override
    public <T> List<T> zrevrangebypage(String key, double startScore, double endScore, int offset, int count, Class<T> clazz) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<String> jsonStrings = scoredSortedSet.valueRangeReversed(startScore, true, endScore, true, offset, count);
        return convertJsonToObjects(jsonStrings, clazz);
    }

    @Override
    public Integer zrank(String key, Object value) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        String jsonString = JsonUtils.object2Json(value);
        return scoredSortedSet.rank(jsonString);
    }

    @Override
    public Integer zrevrank(String key, Object value) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        String jsonString = JsonUtils.object2Json(value);
        return scoredSortedSet.revRank(jsonString);
    }

    @Override
    public boolean zrem(String key, Object value) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        String jsonString = JsonUtils.object2Json(value);
        return scoredSortedSet.remove(jsonString);
    }

    @Override
    public int zremrangebyrank(String key, int startIndex, int endIndex) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.removeRangeByRank(startIndex, endIndex);
    }

    @Override
    public int zremrangebyscore(String key, double startScore, double endScore) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.removeRangeByScore(startScore, true, endScore, true);
    }

    /**
     * json转对象
     *
     * @param jsonStrings json字符串列表
     * @param clazz 类型
     * @return 对象集合
     */
    private <T> List<T> convertJsonToObjects(Collection<String> jsonStrings, Class<T> clazz) {
        if (CollectionUtils.isEmpty(jsonStrings)) {
            return new ArrayList<>(0);
        }
        return jsonStrings.stream()
                .map(jsonString -> JsonUtils.json2Object(jsonString, clazz))
                .collect(Collectors.toList());
    }
}
