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
package wang.bigbird.domain.framework.core.base.util.task;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * 子任务，具体的子任务要继承该类并且实现获取子任务数据集与处理具体数据的业务逻辑
 *
 * @author Bigbird
 */
@Slf4j
public abstract class AbstractChildTask {

    /**
     * 任务名称
     */
    private String taskName;
    /**
     * 线程池大小
     */
    private int poolSize = 3;
    /**
     * 数据拆分大小
     */
    private int splitSize = 5;

    /**
     * 接收jvm关闭信号，实现优雅停机
     */
    private volatile boolean terminal = false;

    public AbstractChildTask(String taskName, int poolSize, int splitSize) {
        this.taskName = taskName;
        this.poolSize = poolSize;
        this.splitSize = splitSize;
    }

    /**
     * 执行任务
     *
     * @param <T>
     */
    public <T> void doExecute() {
        int i = 0;
        while (true) {
            log.debug("{}: Cycle-{}-Begin.", taskName, i);
            // 获取数据
            List<T> datas = queryData();
            // 处理数据
            taskExecute(datas);
            log.debug("{}: Cycle-{}-End.", taskName, i);
            if (terminal) {
                // 只有应用关闭，才会走到这里，用于实现优雅的下线
                break;
            }
            if (CollectionUtils.isNotEmpty(datas)) {
                i++;
                datas.clear();
            } else {
                try {
                    Thread.sleep(1000L);
                } catch (Exception e) {
                    log.error("DoExecute:", e);
                }
            }
        }
        // 回收线程池资源
        TaskProcessUtils.releaseExecutors(taskName);
    }

    /**
     * 优雅停机
     */
    public void terminal() {
        // 关机
        terminal = true;
        log.debug("{} shutdown.", taskName);
    }

    /**
     * 处理数据
     *
     * @param datas
     * @param latch
     * @param <T>
     */
    private <T> void processData(List<T> datas, CountDownLatch latch) {
        try {
            for (T data : datas) {
                doProcessData(data);
            }
        } catch (Exception e) {
            log.error("DoProcessData:", e);
        } finally {
            if (latch != null) {
                latch.countDown();
            }
        }
    }

    /**
     * 处理子任务数据
     *
     * @param sourceDatas 子任务数据
     * @param <T>
     */
    private <T> void taskExecute(List<T> sourceDatas) {
        if (CollectionUtils.isEmpty(sourceDatas)) {
            return;
        }
        // 将数据拆成多份
        List<List<T>> splitDatas = Lists.partition(sourceDatas, splitSize);
        final CountDownLatch latch = new CountDownLatch(splitDatas.size());
        // 并发处理拆分的数据，共用一个线程池
        for (final List<T> datas : splitDatas) {
            ExecutorService executorService = TaskProcessUtils.getOrInitExecutors(taskName, poolSize);
            executorService.submit(() -> processData(datas, latch));
        }
        try {
            latch.await();
        } catch (Exception e) {
            log.error("TaskExecute:", e);
        }
    }

    /**
     * 处理任务单个数据
     *
     * @param data
     * @param <T>
     */
    protected abstract <T> void doProcessData(T data);

    /**
     * 获取永动任务数据集
     *
     * @return
     */
    protected abstract <T> List<T> queryData();
}
