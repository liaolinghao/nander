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
package wang.bigbird.domain.framework.id.support.strategy.meituan.leaf.segment;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import wang.bigbird.domain.framework.id.domain.entity.LeafAlloc;
import wang.bigbird.domain.framework.id.exception.IdGenerateException;
import wang.bigbird.domain.framework.id.service.db.ILeafAllocService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 美团Leaf-segment方式双buffer优化后的ID生成器
 *
 * @author Bigbird
 */
@Slf4j
public class SegmentIdWorker {

    public static class UpdateThreadFactory implements ThreadFactory {

        private static int threadInitNumber = 0;

        private static synchronized int nextThreadNum() {
            return threadInitNumber++;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Thread-Segment-Update-" + nextThreadNum());
        }
    }

    /**
     * IDCache未初始化成功时的异常
     */
    private static final String EXCEPTION_ID_IDCACHE_INIT_FALSE = "Init id cache failed.";
    /**
     * key不存在时的异常
     */
    private static final String EXCEPTION_ID_KEY_NOT_EXISTS = "The biz tag not existed.";
    /**
     * SegmentBuffer中的两个Segment均未从DB中装载时的异常
     */
    private static final String EXCEPTION_ID_TWO_SEGMENTS_ARE_NULL = "The segment id worker are not ready.";
    /**
     * 最大步长不超过100,0000
     */
    private static final int MAX_STEP = 1000000;
    /**
     * 一个Segment维持时间为15分钟
     */
    private static final long SEGMENT_DURATION = 15 * 60 * 1000L;

    private static final int STEP_MULTIPLIER = 2;

    private ExecutorService service = new ThreadPoolExecutor(5, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), new UpdateThreadFactory());

    private volatile boolean initOk = false;

    /**
     * 业务标识对应的号段池
     */
    @Getter
    private Map<String, SegmentBufferBO> cache = new ConcurrentHashMap<>();

    @Autowired
    private ILeafAllocService leafAllocService;

    @PostConstruct
    public void init() {
        log.info("Init ...");
        // 循环定时更新，首次更新在15秒后执行以便留出flyway创建表的时间
        updateCacheFromDbAtEveryMinute();
    }

    private void updateCacheFromDbAtEveryMinute() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("check-idCache-thread");
            t.setDaemon(true);
            return t;
        });
        service.scheduleWithFixedDelay(() -> updateCacheFromDb(), 15, 60, TimeUnit.SECONDS);
    }

    private void updateCacheFromDb() {
        log.info("Update cache from db.");
        StopWatch sw = new Slf4JStopWatch();
        try {
            List<String> dbTags = leafAllocService.loadBizTag();
            // 将数据库中未放入缓存的业务Segment放入缓存
            loadUnCacheTags(dbTags);
            // 将缓存中包含的不在数据库中的业务标签从缓存中移除
            removeUnUseCacheTags(dbTags);
            initOk = true;
        } catch (Exception e) {
            log.error("UpdateCacheFromDb:", e);
        } finally {
            sw.stop("updateCacheFromDb");
        }
    }

    /**
     * 将缓存中包含的不在数据库中的业务标签从缓存中移除
     *
     * @param dbTags 数据库业务标签集合
     */
    private void removeUnUseCacheTags(List<String> dbTags) {
        List<String> removeTagsSet = new ArrayList<>(cache.keySet());
        for (String tag : dbTags) {
            if (removeTagsSet.contains(tag)) {
                removeTagsSet.remove(tag);
            }
        }
        for (String tag : removeTagsSet) {
            cache.remove(tag);
            log.info("Remove tag {} from IdCache.", tag);
        }
    }

    /**
     * 提取数据库中未载入缓存的业务标签，构造对应的SegmentBuffer后放入缓存
     *
     * @param dbTags 数据库业务标签集合
     */
    private void loadUnCacheTags(List<String> dbTags) {
        for (String tag : dbTags) {
            if (cache.containsKey(tag)) {
                continue;
            }
            SegmentBufferBO buffer = new SegmentBufferBO();
            buffer.setBizTag(tag);
            SegmentBO segment = buffer.getCurrent();
            segment.setValue(new AtomicLong(0));
            segment.setMax(0);
            segment.setStep(0);
            cache.put(tag, buffer);
            log.info("Add tag {} from db to IdCache, SegmentBuffer {}.", tag, buffer);
        }
    }

    public long get(String bizTag) {
        if (!initOk) {
            throw new IdGenerateException(EXCEPTION_ID_IDCACHE_INIT_FALSE);
        }
        if (cache.containsKey(bizTag)) {
            SegmentBufferBO buffer = cache.get(bizTag);
            initSegmentBuffer(bizTag, buffer);
            return getIdFromSegmentBuffer(buffer);
        }
        throw new IdGenerateException(EXCEPTION_ID_KEY_NOT_EXISTS);
    }

    /**
     * 初始化指定业务的号段池，将号段池中的第一个号段进行填充
     *
     * @param bizTag
     * @param buffer
     */
    private void initSegmentBuffer(String bizTag, SegmentBufferBO buffer) {
        if (!buffer.isInitOk()) {
            synchronized (buffer) {
                if (!buffer.isInitOk()) {
                    try {
                        updateSegmentFromDb(bizTag, buffer.getCurrent());
                        log.info("Init buffer. Update leaf bizTag {} {} from db.", bizTag, buffer.getCurrent());
                        buffer.setInitOk(true);
                    } catch (Exception e) {
                        log.warn("Init buffer {} exception.", buffer.getCurrent(), e);
                    }
                }
            }
        }
    }

    /**
     * 根据数据库中对应业务的号码分配信息设置指定号段信息
     *
     * @param bizTag
     * @param segment
     */
    private void updateSegmentFromDb(String bizTag, SegmentBO segment) {
        StopWatch sw = new Slf4JStopWatch();
        SegmentBufferBO buffer = segment.getBuffer();
        LeafAlloc leafAlloc;
        if (!buffer.isInitOk()) {
            // 首次初始化号段池中第一个号段信息
            leafAlloc = leafAllocService.updateMaxIdAndGetLeafAlloc(bizTag);
            buffer.setStep(leafAlloc.getStep());
            buffer.setMinStep(leafAlloc.getStep());
        } else if (buffer.getUpdateTimestamp() == 0) {
            // 首次初始化号段池中下一个号段信息
            leafAlloc = leafAllocService.updateMaxIdAndGetLeafAlloc(bizTag);
            buffer.setUpdateTimestamp(System.currentTimeMillis());
            buffer.setStep(leafAlloc.getStep());
            buffer.setMinStep(leafAlloc.getStep());
        } else {
            // 号段池中双号段相互切换提供号码服务时，号段信息填充方式
            // 该填充方式会根据号码消费速度动态调节号段可提供的号码数量
            long duration = System.currentTimeMillis() - buffer.getUpdateTimestamp();
            int nextStep = buffer.getStep();
            if (duration < SEGMENT_DURATION) {
                // 15分钟内发生了号段信息填充准备，那么扩大号段数量值为原来的2倍
                if (nextStep * STEP_MULTIPLIER > MAX_STEP) {
                    // do nothing
                } else {
                    nextStep = nextStep * STEP_MULTIPLIER;
                }
            } else if (duration < SEGMENT_DURATION * STEP_MULTIPLIER) {
                // do nothing with nextStep
            } else {
                // 超过30分钟才发生了号段信息填充准备，那么缩小号段数量值为原来的一半
                nextStep = nextStep / STEP_MULTIPLIER >= buffer.getMinStep() ? nextStep / STEP_MULTIPLIER : nextStep;
            }
            log.info("Leaf bizTag[{}], step[{}], duration[{} mins], nextStep[{}].", bizTag, buffer.getStep(), String.format("%.2f", ((double) duration / (1000 * 60))), nextStep);
            leafAlloc = leafAllocService.updateMaxIdByCustomStepAndGetLeafAlloc(bizTag, nextStep);
            buffer.setUpdateTimestamp(System.currentTimeMillis());
            buffer.setStep(nextStep);
            buffer.setMinStep(leafAlloc.getStep());
        }
        // 根据号码分配信息设置号段信息
        long value = leafAlloc.getMaxId() - buffer.getStep();
        segment.getValue().set(value);
        segment.setMax(leafAlloc.getMaxId());
        segment.setStep(buffer.getStep());
        sw.stop("updateSegmentFromDb", bizTag + " " + segment);
    }

    /**
     * 从号码池中获取号码，采用读写锁对多线程同时获取号码服务时的各种场景进行了处理
     *
     * @param buffer
     * @return
     */
    private long getIdFromSegmentBuffer(final SegmentBufferBO buffer) {
        while (true) {
            // 如果能从当前段获取一个合理号码，就直接返回
            boolean rLocked = false;
            try {
                buffer.rLock().lock();
                rLocked = true;
                final SegmentBO segment = buffer.getCurrent();
                if (!buffer.isNextReady() && (segment.getIdle() < 0.9 * segment.getStep()) && buffer.getThreadRunning().compareAndSet(false, true)) {
                    // 当前号段已下发10%时，如果下一个号段未更新，则另启一个更新线程去更新下一个号段。
                    service.execute(() -> refreshNextSegment(buffer));
                }
                long value = segment.getValue().getAndIncrement();
                if (value < segment.getMax()) {
                    return value;
                }
            } finally {
                if (rLocked) {
                    // 确保在任何情况下都释放读锁
                    buffer.rLock().unlock();
                }
            }
            // 如果在当前段未取得合法号码，并且发现正在准备下一个号段，那么尝试等待一段时间
            waitAndSleep(buffer);
            boolean wLocked = false;
            try {
                // 多个线程同时到达这里会发生阻塞，此时只有获得锁的线程能继续往下执行，其他线程等待
                buffer.wLock().lock();
                wLocked = true;
                // 如果上一个获得锁的线程进入后，判断下一个号段准备好了，会执行号段切换
                // 此时，等待上一个获得锁的线程释放锁后，新进来的线程就可以从切换后的号段获取号码
                final SegmentBO segment = buffer.getCurrent();
                long value = segment.getValue().getAndIncrement();
                if (value < segment.getMax()) {
                    return value;
                }
                if (buffer.isNextReady()) {
                    // 获得锁的线程判断下一个号段准备好了，就执行号段切换，并在后续循环中从切换后的号段获取号码
                    buffer.switchPos();
                    buffer.setNextReady(false);
                } else {
                    // 到达这里表示当前号段已经消耗完毕并且下一个号段还没有准备好，此时无法提供号码服务了
                    log.error("Both two segments in {} are not ready!", buffer);
                    throw new IdGenerateException(EXCEPTION_ID_TWO_SEGMENTS_ARE_NULL);
                }
            } finally {
                if (wLocked) {
                    // 确保写锁也被正确释放
                    buffer.wLock().unlock();
                }
            }
        }
    }

    /**
     * 填充下一个号段信息
     *
     * @param buffer
     */
    private void refreshNextSegment(SegmentBufferBO buffer) {
        SegmentBO next = buffer.getSegments()[buffer.nextPos()];
        boolean updateOk = false;
        try {
            updateSegmentFromDb(buffer.getBizTag(), next);
            updateOk = true;
            log.info("Update segment {} from db {}.", buffer.getBizTag(), next);
        } catch (Exception e) {
            log.error("{} update segment from db exception.", buffer.getBizTag(), e);
        } finally {
            if (updateOk) {
                boolean locked = false;
                try {
                    buffer.wLock().lock();
                    locked = true;
                    buffer.setNextReady(true);
                    buffer.getThreadRunning().set(false);
                } finally {
                    if (locked) {
                        buffer.wLock().unlock();
                    }
                }
            } else {
                buffer.getThreadRunning().set(false);
            }
        }
    }

    /**
     * 如果刷新下一个号段线程正处于运行状态，那么最多等待一万个时钟周期
     *
     * @param buffer
     */
    private void waitAndSleep(SegmentBufferBO buffer) {
        int roll = 0;
        while (buffer.getThreadRunning().get()) {
            roll += 1;
            if (roll > 10000) {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                    break;
                } catch (InterruptedException e) {
                    log.warn("Thread {} Interrupted", Thread.currentThread().getName());
                    break;
                }
            }
        }
    }

}
