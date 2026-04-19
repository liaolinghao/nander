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
package wang.bigbird.domain.framework.distributedlock.core.support.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.distributedlock.core.support.annotation.Lock;
import wang.bigbird.domain.framework.distributedlock.core.base.util.LockUtils;
import wang.bigbird.domain.framework.distributedlock.core.service.base.IDistributedLockService;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁 Aop
 *
 * @author Bigbird
 */
@Aspect
@Slf4j
@Component
public class DistributedLockAop {

    /**
     * 分布式锁
     */
    @Autowired
    private IDistributedLockService distributedLock;

    /**
     * pointcut 指定切入点的生效时机
     *
     * @annotation 当执行方法时拥有指定注解生效
     */
    @Pointcut("@annotation(wang.bigbird.domain.framework.distributedlock.core.support.annotation.Lock)")
    public void pointcut() {
        // do nothing
    }

    /**
     * lock aop 处理
     *
     * @Around 指示在切入方法的执行前和指定后，做一些增强处理
     */
    @Around(value = "pointcut() && @annotation(lock)")
    public Object around(ProceedingJoinPoint joinPoint, Lock lock) throws Throwable {
        String lockKey = lock.lockKey();
        Assert.hasText(lockKey, "lockKey not null.");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] parameterNames = new LocalVariableTableParameterNameDiscoverer().getParameterNames(method);
        Object[] args = joinPoint.getArgs();
        String lockKeyPrefix = lock.lockKeyPrefix();
        String key = LockUtils.getKeyBySpel(lockKeyPrefix, lockKey, parameterNames, args);
        Assert.hasText(key, "the generate key is not valid.");
        int leaseTimeout = lock.leaseTime();
        TimeUnit timeUnit = lock.timeUnit();
        // 阻塞方式获取锁
        if (leaseTimeout <= 0) {
            distributedLock.lock(key);
        } else {
            distributedLock.lock(key, leaseTimeout, timeUnit);
        }
        try {
            return joinPoint.proceed();
        } finally {
            distributedLock.unlock(key);
        }
    }
}
