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
package wang.bigbird.domain.framework.id.support.strategy.baidu.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import wang.bigbird.domain.framework.id.exception.IdGenerateException;
import wang.bigbird.domain.framework.id.support.strategy.baidu.BitsAllocator;
import wang.bigbird.domain.framework.id.support.strategy.baidu.UidWorker;
import wang.bigbird.domain.framework.id.support.strategy.baidu.buffer.BufferPaddingExecutor;
import wang.bigbird.domain.framework.id.support.strategy.baidu.buffer.RejectedPutBufferHandler;
import wang.bigbird.domain.framework.id.support.strategy.baidu.buffer.RejectedTakeBufferHandler;
import wang.bigbird.domain.framework.id.support.strategy.baidu.buffer.RingBuffer;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a cached implementation of {@link UidWorker} extends
 * from {@link DefaultUidWorker}, based on a lock free {@link RingBuffer}<p>
 * <p>
 * The spring properties you can specified as below:<br>
 * <li><b>boostPower:</b> RingBuffer size boost for a power of 2, Sample: boostPower is 3, it means the buffer size
 * will be <code>({@link BitsAllocator#getMaxSequence()} + 1) &lt;&lt;
 * {@link #boostPower}</code>, Default as {@value #DEFAULT_BOOST_POWER}
 * <li><b>paddingFactor:</b> Represents a percent value of (0 - 100). When the count of rest available UIDs reach the
 * threshold, it will trigger padding buffer. Default as{@link RingBuffer#DEFAULT_PADDING_PERCENT}
 * Sample: paddingFactor=20, bufferSize=1000 -> threshold=1000 * 20 /100, padding buffer will be triggered when tail-cursor<threshold
 * <li><b>scheduleInterval:</b> Padding buffer in a schedule, specify padding buffer interval, Unit as second
 * <li><b>rejectedPutBufferHandler:</b> Policy for rejected put buffer. Default as discard put request, just do logging
 * <li><b>rejectedTakeBufferHandler:</b> Policy for rejected take buffer. Default as throwing up an exception
 *
 * @author Bigbird
 */
@Slf4j
public class CachedUidWorker extends DefaultUidWorker {

    private static final int DEFAULT_BOOST_POWER = 3;

    private int boostPower = DEFAULT_BOOST_POWER;
    private int paddingFactor = RingBuffer.DEFAULT_PADDING_PERCENT;
    private Long scheduleInterval;

    private RejectedPutBufferHandler rejectedPutBufferHandler;
    private RejectedTakeBufferHandler rejectedTakeBufferHandler;

    /**
     * RingBuffer
     */
    private RingBuffer ringBuffer;
    private BufferPaddingExecutor bufferPaddingExecutor;

    public CachedUidWorker(int timeBits, int workerBits, int seqBits, String epochStr, long workerId, int boostPower, int paddingFactor, Long scheduleInterval, RejectedPutBufferHandler rejectedPutBufferHandler, RejectedTakeBufferHandler rejectedTakeBufferHandler) {
        super(timeBits, workerBits, seqBits, epochStr, workerId);
        Assert.isTrue(boostPower > 0, "Boost power must be positive!");
        Assert.isTrue(paddingFactor > 0 && paddingFactor < 100, "Padding factor must be greater than 0 and less than 100!");
        this.boostPower = boostPower;
        this.paddingFactor = paddingFactor;
        this.scheduleInterval = scheduleInterval;
        this.rejectedPutBufferHandler = rejectedPutBufferHandler;
        this.rejectedTakeBufferHandler = rejectedTakeBufferHandler;
        initRingBuffer();
    }

    @Override
    public long getUid() {
        try {
            return ringBuffer.take();
        } catch (Exception e) {
            log.error("Generate unique id exception.", e);
            throw new IdGenerateException(e);
        }
    }

    @Override
    public String parseUid(long uid) {
        return super.parseUid(uid);
    }

    @PreDestroy
    public void destroy() {
        bufferPaddingExecutor.shutdown();
    }

    /**
     * Get the UIDs in the same specified second under the max sequence
     *
     * @param currentSecond
     * @return UID list, size of {@link BitsAllocator#getMaxSequence()} + 1
     */
    protected List<Long> nextIdsForOneSecond(long currentSecond) {
        // Initialize result list size of (max sequence + 1)
        int listSize = (int) bitsAllocator.getMaxSequence() + 1;
        List<Long> uidList = new ArrayList<>(listSize);
        // Allocate the first sequence of the second, the others can be calculated with the offset
        long firstSeqUid = bitsAllocator.allocate(currentSecond - epochSeconds, workerId, 0L);
        for (int offset = 0; offset < listSize; offset++) {
            uidList.add(firstSeqUid + offset);
        }
        return uidList;
    }

    /**
     * Initialize RingBuffer & RingBufferPaddingExecutor
     */
    private void initRingBuffer() {
        // initialize RingBuffer
        int bufferSize = ((int) bitsAllocator.getMaxSequence() + 1) << boostPower;
        this.ringBuffer = new RingBuffer(bufferSize, paddingFactor);
        log.info("Initialized ring buffer size: {}, paddingFactor: {}.", bufferSize, paddingFactor);
        // initialize RingBufferPaddingExecutor
        boolean usingSchedule = (scheduleInterval != null);
        this.bufferPaddingExecutor = new BufferPaddingExecutor(ringBuffer, this::nextIdsForOneSecond, usingSchedule);
        if (usingSchedule) {
            bufferPaddingExecutor.setScheduleInterval(scheduleInterval);
        }
        log.info("Initialized BufferPaddingExecutor, using schdule: {}, interval: {}.", usingSchedule, scheduleInterval);
        // set rejected put/take handle policy
        this.ringBuffer.setBufferPaddingExecutor(bufferPaddingExecutor);
        if (rejectedPutBufferHandler != null) {
            this.ringBuffer.setRejectedPutHandler(rejectedPutBufferHandler);
        }
        if (rejectedTakeBufferHandler != null) {
            this.ringBuffer.setRejectedTakeHandler(rejectedTakeBufferHandler);
        }
        // fill in all slots of the RingBuffer
        bufferPaddingExecutor.paddingBuffer();
        // start buffer padding threads
        bufferPaddingExecutor.start();
    }

}
