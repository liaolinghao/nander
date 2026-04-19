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

import org.redisson.api.RDeque;
import org.redisson.api.RList;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.data.redis.service.base.IRedisListService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * redis list 服务
 *
 * @author Bigbird
 */
@Service
public class RedisListServiceImpl implements IRedisListService {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void lpush(String key, Object value) {
        RDeque<String> deque = redissonClient.getDeque(key);
        String jsonString = JsonUtils.object2Json(value);
        deque.addFirst(jsonString);
    }

    @Override
    public <T> T lpop(String key, Class<T> clazz) {
        RQueue<String> queue = redissonClient.getQueue(key);
        String jsonString = queue.poll();
        return JsonUtils.json2Object(jsonString, clazz);
    }

    @Override
    public void rpush(String key, Object value) {
        RList<String> list = redissonClient.getList(key);
        String jsonString = JsonUtils.object2Json(value);
        list.add(jsonString);
    }

    @Override
    public <T> T rpop(String key, Class<T> clazz) {
        RDeque<String> deque = redissonClient.getDeque(key);
        String jsonString = deque.pollLast();
        return JsonUtils.json2Object(jsonString, clazz);
    }

    @Override
    public <T> T lindex(String key, int index, Class<T> clazz) {
        RList<String> list = redissonClient.getList(key);
        String jsonString = list.get(index);
        return JsonUtils.json2Object(jsonString, clazz);
    }

    @Override
    public int llen(String key) {
        RList<String> list = redissonClient.getList(key);
        return list.size();
    }

    @Override
    public <T> List<T> lrange(String key, int start, int end, Class<T> clazz) {
        RList<String> list = redissonClient.getList(key);
        List<String> range = list.range(start, end);
        return range.stream()
                .map(jsonString -> JsonUtils.json2Object(jsonString, clazz))
                .collect(Collectors.toList());
    }

    @Override
    public boolean lrem(String key, int count, Object value) {
        RList<String> list = redissonClient.getList(key);
        String jsonString = JsonUtils.object2Json(value);
        return list.remove(jsonString, count);
    }

    @Override
    public <T> T rpoplpush(String sourceKey, String destKey, Class<T> clazz) {
        RDeque<String> deque = redissonClient.getDeque(sourceKey);
        String jsonString = deque.pollLastAndOfferFirstTo(destKey);
        return JsonUtils.json2Object(jsonString, clazz);
    }
}
