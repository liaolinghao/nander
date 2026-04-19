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

import cn.hutool.core.thread.ThreadFactoryBuilder;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.*;

/**
 * 永动机，用于执行循环任务（指不发送停止命令，就永远执行下去的任务）
 * <p>
 * 可执行多个不同类别的循环任务，具体的永动机需要继承该类并且实现初始化子任务集合的业务逻辑
 *
 * @author Bigbird
 */
public abstract class AbstractLoopTask {

    /**
     * 子任务集合
     */
    private List<AbstractChildTask> childTasks;

    /**
     * 线程池
     */
    private ExecutorService executorService;

    /**
     * 初始化子任务集合
     */
    public abstract void initLoopTask();

    /**
     * 开启永动机任务
     */
    public void startLoopTask() {
        // 根据子任务数量创建固定大小的线程池
        int poolSize = childTasks != null ? childTasks.size() : 1;
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNamePrefix("childTask-thread-").build();
        executorService = new ThreadPoolExecutor(poolSize, poolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());
        for (final AbstractChildTask childTask : childTasks) {
            executorService.execute(() -> childTask.doExecute());
        }
    }

    /**
     * 关闭永动机任务
     */
    public void shutdownLoopTask() {
        if (CollectionUtils.isNotEmpty(childTasks)) {
            for (AbstractChildTask childTask : childTasks) {
                childTask.terminal();
            }
        }
        if (executorService != null) {
            // 优雅关闭线程池
            executorService.shutdown();
        }
    }

}
