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
package wang.bigbird.domain.framework.distributedlock.redislock.service.base.impl;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.distributedlock.core.service.base.IDistributedLockService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * redis 锁服务实现类
 *
 * @author Bigbird
 */
@Service
public class RedisLockServiceImpl implements IDistributedLockService {

    /**
     * 同一个线程可能持有多个不同的分布式锁，采用Map<String, RLock>进行记录
     * 同时采用ThreadLocal记录线程自己持有的锁信息
     */
    private static final ThreadLocal<Map<String, RLock>> RLOCK_THREAD_LOCAL = new ThreadLocal<>();


    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void lock(String lockKey) throws InterruptedException {
        RLock lock = redissonClient.getLock(lockKey);
        // 已经获得锁，不需要再锁定
        if (isHasLock(lockKey)) {
            return;
        }
        // 拿不到锁，当前线程会一直等待，除非拿到锁或者当前线程被其他原因中断
        lock.lockInterruptibly();
        recordLock(lockKey, lock);
    }

    @Override
    public void lock(String lockKey, long leaseTime, TimeUnit unit) throws InterruptedException {
        RLock lock = redissonClient.getLock(lockKey);
        // 拿不到锁，当前线程会一直等待，除非拿到锁或者当前线程被其他原因中断
        lock.lockInterruptibly(leaseTime, unit);
        recordLock(lockKey, lock);
    }

    @Override
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = lock.tryLock(waitTime, leaseTime, timeUnit);
        if (acquired) {
            recordLock(lockKey, lock);
        }
        return acquired;
    }

    @Override
    public void unlock(String lockKey) {
        if (!isHasLock(lockKey)) {
            return;
        }
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isLocked()) {
            // 是否还是锁定状态
            if (lock.isHeldByCurrentThread()) {
                // 是当前执行线程的锁释放锁
                try {
                    lock.unlock();
                } finally {
                    removeLock(lockKey);
                }
            }
        }
    }

    /**
     * 删除锁
     *
     * @param lockKey 锁键
     */
    private void removeLock(String lockKey) {
        Map<String, RLock> mutexMap = RLOCK_THREAD_LOCAL.get();
        if (mutexMap != null && mutexMap.containsKey(lockKey)) {
            mutexMap.remove(lockKey);
        }
    }

    /**
     * 记录锁
     *
     * @param lockKey 锁键
     * @param mutex   互斥锁
     */
    private void recordLock(String lockKey, RLock mutex) {
        Map<String, RLock> mutexMap = RLOCK_THREAD_LOCAL.get();
        if (mutexMap == null) {
            mutexMap = new HashMap<>(CollectionUtils.initialMapCapacity(12));
            RLOCK_THREAD_LOCAL.set(mutexMap);
        }
        if (!mutexMap.containsKey(lockKey)) {
            mutexMap.put(lockKey, mutex);
        }
    }

    /**
     * 当前线程是否已经获得该锁
     *
     * @param lockKey 锁键
     * @return
     */
    private boolean isHasLock(String lockKey) {
        Map<String, RLock> mutexMap = RLOCK_THREAD_LOCAL.get();
        return null != mutexMap && mutexMap.containsKey(lockKey);
    }

}
