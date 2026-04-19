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
package wang.bigbird.domain.framework.distributedlock.core.service.base;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁
 *
 * @author Bigbird
 */
public interface IDistributedLockService {

    /**
     * 获取锁，并且一直持有锁
     *
     * @param lockKey 锁键
     * @throws InterruptedException
     */
    void lock(String lockKey) throws InterruptedException;

    /**
     * 获取锁，经过指定时间后，释放锁
     *
     * @param lockKey   锁键
     * @param leaseTime 持有时长
     * @param timeUnit  时间单位
     * @throws InterruptedException
     */
    void lock(String lockKey, long leaseTime, TimeUnit timeUnit) throws InterruptedException;

    /**
     * 尝试获取锁（ZK锁暂时不支持）
     *
     * @param lockKey   锁键
     * @param waitTime  等待时长
     * @param leaseTime 持有时长
     * @param timeUnit  时间单位
     * @return 是否获取成功
     * @throws InterruptedException
     */
    boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException;

    /**
     * 释放锁
     *
     * @param lockKey 锁键
     */
    void unlock(String lockKey);

}
