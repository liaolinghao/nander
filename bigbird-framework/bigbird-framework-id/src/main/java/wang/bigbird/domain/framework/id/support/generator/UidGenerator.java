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
package wang.bigbird.domain.framework.id.support.generator;

import wang.bigbird.domain.framework.id.base.enums.IdStrategyEnum;
import wang.bigbird.domain.framework.id.support.strategy.baidu.UidWorker;
import wang.bigbird.domain.framework.id.support.strategy.baidu.buffer.RejectedPutBufferHandler;
import wang.bigbird.domain.framework.id.support.strategy.baidu.buffer.RejectedTakeBufferHandler;
import wang.bigbird.domain.framework.id.support.strategy.baidu.impl.CachedUidWorker;
import wang.bigbird.domain.framework.id.support.assigner.WorkerIdAssigner;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于baidu-uid策略的ID生成器
 *
 * @author Bigbird
 */
public class UidGenerator implements IdGenerator {

    /**
     * 生成器集合
     */
    private static Map<String, UidWorker> generatorMap = new HashMap<>();

    /**
     * 时间戳部分长度
     */
    private int timeBits;
    /**
     * 机器ID部分长度
     */
    private int workerBits;
    /**
     * 序列号部分长度
     */
    private int seqBits;
    /**
     * 起始日期
     */
    private String epochStr;
    /**
     * worker id 分配器
     */
    private WorkerIdAssigner workerIdAssigner;
    /**
     * RingBuffer size扩容参数，可提高UID生成的吞吐量，
     * 默认:3，原bufferSize=8192，扩容后bufferSize= 8192 << 3 = 65536
     */
    private int boostPower;
    /**
     * 指定何时向RingBuffer中填充UID，取值为百分比(0, 100)，默认为50
     * 举例：bufferSize=1024，paddingFactor=50 -> threshold=1024 * 50 / 100 = 512
     * 当环上可用UID数量 < 512时，将自动对RingBuffer进行填充补全
     */
    private int paddingFactor;
    /**
     * 另外一种RingBuffer填充时机，在Schedule线程中周期性检查填充
     * 默认:不配置此项，即不实用Schedule线程。如需使用，请指定Schedule线程时间间隔，单位:秒
     */
    private Long scheduleInterval;
    /**
     * 拒绝策略: 当环已满，无法继续填充时
     * 默认无需指定，将丢弃Put操作，仅日志记录。如有特殊需求，请实现RejectedPutBufferHandler接口（支持Lambda表达式）
     */
    private RejectedPutBufferHandler rejectedPutBufferHandler;
    /**
     * 拒绝策略: 当环已空, 无法继续获取时
     * 默认无需指定，将记录日志并抛出IdGenerateException异常。如有特殊需求，请实现RejectedTakeBufferHandler接口（支持Lambda表达式）
     */
    private RejectedTakeBufferHandler rejectedTakeBufferHandler;

    public UidGenerator(int timeBits, int workerBits, int seqBits, String epochStr, WorkerIdAssigner workerIdAssigner, int boostPower, int paddingFactor, Long scheduleInterval, RejectedPutBufferHandler rejectedPutBufferHandler, RejectedTakeBufferHandler rejectedTakeBufferHandler) {
        this.timeBits = timeBits;
        this.workerBits = workerBits;
        this.seqBits = seqBits;
        this.epochStr = epochStr;
        this.workerIdAssigner = workerIdAssigner;
        this.boostPower = boostPower;
        this.paddingFactor = paddingFactor;
        this.scheduleInterval = scheduleInterval;
        this.rejectedPutBufferHandler = rejectedPutBufferHandler;
        this.rejectedTakeBufferHandler = rejectedTakeBufferHandler;
    }

    @Override
    public IdStrategyEnum getStrategy() {
        return IdStrategyEnum.uid;
    }

    @Override
    public long getUid(String bizTag) {
        return getUidWorker(bizTag).getUid();
    }

    @Override
    public String parseUid(long uid, String bizTag) {
        return getUidWorker(bizTag).parseUid(uid);
    }

    private UidWorker getUidWorker(String bizTag) {
        UidWorker uidWorker = generatorMap.get(bizTag);
        if (null == uidWorker) {
            synchronized (generatorMap) {
                uidWorker = generatorMap.get(bizTag);
                if (null == uidWorker) {
                    uidWorker = new CachedUidWorker(timeBits, workerBits, seqBits, epochStr, workerIdAssigner.assignWorkerId(), boostPower, paddingFactor, scheduleInterval, rejectedPutBufferHandler, rejectedTakeBufferHandler);
                }
                generatorMap.put(bizTag, uidWorker);
            }
        }
        return uidWorker;
    }

}
