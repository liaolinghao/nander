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
import lombok.Setter;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 号段池 - 双buffer，持有两个号段
 *
 * @author Bigbird
 */
public class SegmentBufferBO {

    @Getter
    @Setter
    private String bizTag;
    /**
     * 号段组
     */
    @Getter
    private SegmentBO[] segments;
    /**
     * 当前使用的号段位于号段组的位置编号
     */
    @Getter
    private volatile int currentPos;
    /**
     * 下一个号段是否处于可切换状态
     */
    @Getter
    @Setter
    private volatile boolean nextReady;
    /**
     * 是否初始化完成
     */
    @Getter
    @Setter
    private volatile boolean initOk;
    /**
     * 线程是否在运行中
     */
    @Getter
    private final AtomicBoolean threadRunning;

    private final ReadWriteLock lock;

    /**
     * 号段池号段可提供的号码数量，可根据消费速度动态调整大小
     */
    @Getter
    @Setter
    private volatile int step;
    /**
     * 号段池号段可提供的最小号码数量，该值依赖数据库设置的数量
     */
    @Getter
    @Setter
    private volatile int minStep;
    /**
     * 号段池初始化完毕后，每次发生号段信息更新的时间
     */
    @Getter
    @Setter
    private volatile long updateTimestamp;

    public SegmentBufferBO() {
        segments = new SegmentBO[]{new SegmentBO(this), new SegmentBO(this)};
        currentPos = 0;
        nextReady = false;
        initOk = false;
        threadRunning = new AtomicBoolean(false);
        // 读写锁允许同一时刻被多个读线程访问，但是在写线程访问时，所有的读线程和其他的写线程都会被阻塞
        lock = new ReentrantReadWriteLock();
    }

    public SegmentBO getCurrent() {
        return segments[currentPos];
    }

    public int nextPos() {
        return (currentPos + 1) % 2;
    }

    public void switchPos() {
        currentPos = nextPos();
    }

    public Lock rLock() {
        return lock.readLock();
    }

    public Lock wLock() {
        return lock.writeLock();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SegmentBuffer{");
        sb.append("bizTag='").append(bizTag).append('\'');
        sb.append(", segments=").append(Arrays.toString(segments));
        sb.append(", currentPos=").append(currentPos);
        sb.append(", nextReady=").append(nextReady);
        sb.append(", initOk=").append(initOk);
        sb.append(", threadRunning=").append(threadRunning);
        sb.append(", step=").append(step);
        sb.append(", minStep=").append(minStep);
        sb.append(", updateTimestamp=").append(updateTimestamp);
        sb.append('}');
        return sb.toString();
    }
}
