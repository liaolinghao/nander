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
package wang.bigbird.domain.framework.core.base.tool;

import java.sql.Timestamp;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 高并发场景下System.currentTimeMillis()的性能问题的优化
 * <p>
 * System.currentTimeMillis()以毫秒为单位返回当前时间。
 * 请注意，虽然返回值的时间单位是毫秒，但值的粒度取决于底层操作系统，并且可能更大。
 * 例如，许多操作系统以几十毫秒为单位测量时间。说明该方法存在时间误差，有精度问题，大概误差在几十毫秒内，因操作系统而异。
 * <p>
 * System.currentTimeMillis()在高并发场景下之所以慢是因为去跟系统打了一次交道，
 * 而系统只有一个全局时钟源，高并发或频繁访问会造成严重的争用，因此可采用如下三种策略优化：
 * <p>
 * 一、如果对时间精确度要求不高的话可以使用独立线程缓存时间戳：如果我们的误差允许在1ms内，那我们保证在1ms内只调用一次System.currentTimeMillis()，
 * 在1ms内的其他调用都直接使用这次调用的结果这样就大大避免了和其他线程抢夺资源的概率。
 * 也减少了线程上下文的切换，以及用户态到内核态的切换。
 * <p>
 * 二、使用Linux的clock_gettime()方法：使用JNI自己实现它。
 * <p>
 * 三、使用System.nanoTime()
 *
 * 本类采用第一种策略实现
 *
 * @author Bigbird
 */
public class SystemClock {

    /**
     * 线程名--系统时钟
     */
    public static final String THREAD_CLOCK_NAME = "System Clock";

    private final long period;

    private final AtomicLong now;

    private SystemClock(long period) {
        this.period = period;
        this.now = new AtomicLong(System.currentTimeMillis());
        scheduleClockUpdating();
    }

    private static class InstanceHolder {
        public static final SystemClock INSTANCE = new SystemClock(1);
    }

    private static SystemClock instance() {
        return InstanceHolder.INSTANCE;
    }

    private void scheduleClockUpdating() {
        ScheduledExecutorService scheduledpool = new ScheduledThreadPoolExecutor(1, new NamingThreadFactory(THREAD_CLOCK_NAME, true, null));
        scheduledpool.scheduleAtFixedRate(() -> {
            now.set(System.currentTimeMillis());
        }, period, period, TimeUnit.MILLISECONDS);
    }

    private long currentTimeMillis() {
        return now.get();
    }

    /**
     * 获取当前时间毫秒值
     *
     * @return
     */
    public static long now() {
        return instance().currentTimeMillis();
    }

    /**
     * 获取当前时间戳
     *
     * @return
     */
    public static String nowDate() {
        return new Timestamp(instance().currentTimeMillis()).toString();
    }
}
