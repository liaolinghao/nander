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
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.data.redis.service.base.IRedisService;

import java.util.concurrent.TimeUnit;

/**
 * redis 基础服务
 *
 * @author Bigbird
 */
@Slf4j
@Service
public class RedisServiceImpl implements IRedisService {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void set(String key, Object value) {
        String jsonString = JsonUtils.object2Json(value);
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(jsonString);
    }

    @Override
    public void set(String key, Object value, long expire, TimeUnit timeUnit) {
        String jsonString = JsonUtils.object2Json(value);
        RBucket<String> bucket = getBucket(key);
        bucket.set(jsonString, expire, timeUnit);
    }

    @Override
    public String get(String key) {
        return get(key, String.class);
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        RBucket<String> bucket = getBucket(key);
        String jsonString = bucket.get();
        return JsonUtils.json2Object(jsonString, clazz);
    }

    @Override
    public boolean expire(String key, long expire, TimeUnit timeUnit) {
        RBucket<String> bucket = getBucket(key);
        return bucket.expire(expire, timeUnit);
    }

    @Override
    public boolean del(String key) {
        RBucket<String> bucket = getBucket(key);
        return bucket.delete();
    }

    @Override
    public boolean setnx(String key, Object value) {
        String jsonString = JsonUtils.object2Json(value);
        RBucket<String> bucket = getBucket(key);
        return bucket.trySet(jsonString);
    }

    @Override
    public boolean setnxex(String key, Object value, long expire, TimeUnit timeUnit) {
        String jsonString = JsonUtils.object2Json(value);
        RBucket<String> bucket = getBucket(key);
        return bucket.trySet(jsonString, expire, timeUnit);
    }

    @Override
    public long incr(String key) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.incrementAndGet();
    }

    @Override
    public long incrby(String key, long delta) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.addAndGet(delta);
    }

    @Override
    public long decr(String key) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.decrementAndGet();
    }

    @Override
    public long decrby(String key, long delta) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.addAndGet(-1 * delta);
    }

    @Override
    public long ttl(String key) {
        RBucket<String> bucket = getBucket(key);
        return bucket.remainTimeToLive();
    }

    @Override
    public boolean exists(String key) {
        RBucket<String> bucket = getBucket(key);
        return bucket.isExists();
    }

    private RBucket<String> getBucket(String key) {
        return redissonClient.getBucket(key);
    }
}
