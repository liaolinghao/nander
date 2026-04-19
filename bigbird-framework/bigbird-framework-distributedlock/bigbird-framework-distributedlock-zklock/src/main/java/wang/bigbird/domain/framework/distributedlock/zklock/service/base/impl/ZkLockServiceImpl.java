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
package wang.bigbird.domain.framework.distributedlock.zklock.service.base.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.distributedlock.core.service.base.IDistributedLockService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * zk 锁服务实现类
 *
 * @author Bigbird
 */
@Service
@Slf4j
public class ZkLockServiceImpl implements IDistributedLockService {

    /**
     * 同一个线程可能持有多个不同的分布式锁，采用Map<String, InterProcessMutex>进行记录
     * 同时采用ThreadLocal记录线程自己持有的锁信息
     */
    private static final ThreadLocal<Map<String, InterProcessMutex>> MUTEX_THREAD_LOCAL = new ThreadLocal<>();

    @Autowired
    private CuratorFramework curatorFramework;

    @Override
    public void lock(String lockKey) throws InterruptedException {
        lockKey = solveLockKeyStartWithSeparator(lockKey);
        // 已经获得锁，不需要再锁定
        if (isHasLock(lockKey)) {
            return;
        }
        InterProcessMutex mutex = new InterProcessMutex(curatorFramework, lockKey);
        try {
            mutex.acquire();
            recordLock(lockKey, mutex);
        } catch (Exception e) {
            log.error("Zookeeper lock error.", e);
            throw new InterruptedException();
        }
    }

    @Override
    public void lock(String lockKey, long leaseTime, TimeUnit unit) throws InterruptedException {
        lockKey = solveLockKeyStartWithSeparator(lockKey);
        // 已经获得锁，不需要再锁定
        if (isHasLock(lockKey)) {
            return;
        }
        InterProcessMutex mutex = new InterProcessMutex(curatorFramework, lockKey);
        try {
            mutex.acquire(leaseTime, unit);
            recordLock(lockKey, mutex);
        } catch (Exception e) {
            log.error("Zookeeper lock error.", e);
            throw new InterruptedException();
        }
    }

    @Override
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("zk lock is not support tryLock.");
    }

    @Override
    public void unlock(String lockKey) {
        lockKey = solveLockKeyStartWithSeparator(lockKey);
        if (!isHasLock(lockKey)) {
            return;
        }
        Map<String, InterProcessMutex> mutexMap = MUTEX_THREAD_LOCAL.get();
        InterProcessMutex mutex = mutexMap.get(lockKey);
        if (mutex.isOwnedByCurrentThread()) {
            try {
                mutex.release();
                removeLock(lockKey);
            } catch (Exception e) {
                log.error("Zookeeper unlock error.", e);
            }
        }
    }

    /**
     * 删除锁
     *
     * @param lockKey 锁键
     */
    private void removeLock(String lockKey) {
        Map<String, InterProcessMutex> mutexMap = MUTEX_THREAD_LOCAL.get();
        if (mutexMap != null && mutexMap.containsKey(lockKey)) {
            mutexMap.remove(lockKey);
        }
    }

    /**
     * 记录锁
     *
     * @param lockKey 锁键
     * @param mutex 互斥锁
     */
    private void recordLock(String lockKey, InterProcessMutex mutex) {
        Map<String, InterProcessMutex> mutexMap = MUTEX_THREAD_LOCAL.get();
        if (mutexMap == null) {
            mutexMap = new HashMap<>(CollectionUtils.initialMapCapacity(12));
            MUTEX_THREAD_LOCAL.set(mutexMap);
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
        Map<String, InterProcessMutex> mutexMap = MUTEX_THREAD_LOCAL.get();
        return null != mutexMap && mutexMap.containsKey(lockKey);
    }

    /**
     * 处理锁键，锁键统一以/开头
     *
     * @param lockKey 锁键
     * @return
     */
    private String solveLockKeyStartWithSeparator(String lockKey) {
        if (lockKey.startsWith(CommonConstants.SLASH)) {
            return lockKey;
        }
        return CommonConstants.SLASH + lockKey;
    }
}
