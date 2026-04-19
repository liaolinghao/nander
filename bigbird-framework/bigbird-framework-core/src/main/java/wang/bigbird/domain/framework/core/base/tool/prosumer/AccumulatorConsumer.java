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
package wang.bigbird.domain.framework.core.base.tool.prosumer;

import com.google.common.collect.Queues;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 累加消费器，当缓存数据超过指定数量或者缓存时间超过指定时间，就一次性消费缓存中的数据
 *
 * @author Bigbird
 */
@Slf4j
public class AccumulatorConsumer<T> implements Runnable {

    /**
     * 最大数据量
     */
    private int maxSize = 100;
    /**
     * 最大缓存时间，毫秒为单位
     */
    private int timeOut = 500;
    /**
     * 数据缓存器
     */
    private final BlockingQueue<T> blockingQueue;
    /**
     * 批量数据
     */
    private final List<T> batchList;
    /**
     * 数据消费器
     */
    private final Consumer<List<T>> consumer;
    /**
     * 数据生产器
     */
    private final IProducer producer;

    public AccumulatorConsumer(int maxSize, int timeOut, BlockingQueue<T> blockingQueue, Consumer<List<T>> consumer, IProducer producer) {
        this.maxSize = maxSize;
        this.timeOut = timeOut;
        this.blockingQueue = blockingQueue;
        this.consumer = consumer;
        this.producer = producer;
        this.batchList = new ArrayList<>(maxSize);
    }

    @Override
    public void run() {
        while (producer.isRunning()) {
            try {
                batchList.clear();
                Queues.drain(blockingQueue, batchList, maxSize, timeOut, TimeUnit.MILLISECONDS);
                consumer.accept(batchList);
            } catch (Exception e) {
                log.error("AccumulatorConsumer:", e);
            }
        }
    }

    /**
     * 强制消费，一般用于任务终止时，将缓存中的剩余数据消费完毕
     */
    public void forceConsume() {
        batchList.clear();
        blockingQueue.drainTo(batchList);
        consumer.accept(batchList);
    }

}
