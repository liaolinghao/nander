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
package wang.bigbird.domain.framework.id.support.assigner;

import wang.bigbird.domain.framework.core.base.tool.NamingThreadFactory;
import wang.bigbird.domain.framework.core.base.tool.SystemClock;
import wang.bigbird.domain.framework.id.base.util.WorkerIdUtils;
import wang.bigbird.domain.framework.id.exception.IdGenerateException;
import wang.bigbird.domain.framework.id.service.base.IPidNameLoaderService;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.ServerSocket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WorkerId生成基类
 *
 * @author Bigbird
 */
public abstract class AbstractWorkerIdAssigner implements WorkerIdAssigner {

    /**
     * 线程名-心跳
     */
    private static final String THREAD_HEARTBEAT_NAME = "id_heartbeat";

    private ServerSocket socket;

    /**
     * 心跳原子标识
     */
    protected AtomicBoolean active = new AtomicBoolean(false);

    /**
     * 心跳间隔
     */
    protected Long interval;

    /**
     * 本地workerID文件存储根路径
     */
    private String pidHome;

    /**
     * 使用端口（同机多uid应用时区分端口）
     */
    private Integer pidPort = -1;

    /**
     * 名称格式为：ip_port
     */
    protected String pidName;

    protected Long workerId;

    private IPidNameLoaderService pidNameLoaderService;

    public AbstractWorkerIdAssigner(Long interval, String pidHome, Integer pidPort, IPidNameLoaderService pidNameLoaderService) {
        this.interval = interval;
        this.pidHome = pidHome;
        this.pidPort = pidPort;
        this.pidNameLoaderService = pidNameLoaderService;
    }

    /**
     * 在初始化workerId的过程中，会执行如下处理：
     * 1、判断之前是否分配了workerId，如果已经分配，那么继续使用上一次分配的workerId，否则新分配一个workerId。
     * 2、判断当前系统时间与上一次上报的时间，如果小于上一次上报的时间，意味着发生了时钟回拨，失败告警。
     * 3、判断当前系统时间与其余节点的系统时间平均值，如果小于平均值，意味着发生了时钟回拨，失败告警。
     *
     * @throws Exception
     */
    @PostConstruct
    public void init() throws Exception {
        try {
            initEnv();
            if (pidNameLoaderService == null) {
                pidName = WorkerIdUtils.getPidName(pidPort, socket);
            } else {
                pidName = pidNameLoaderService.loadPidName();
            }
            workerId = loadWorkerId();
            long timestamp = SystemClock.now();
            long lastTimestamp = lastTime();
            if (timestamp < lastTimestamp) {
                throw new IdGenerateException(String.format(IdGenerateException.ERROR_CLOCK_BACK, lastTimestamp - timestamp));
            }
            long averageTimestamp = averageTime();
            if (timestamp < averageTimestamp) {
                throw new IdGenerateException(String.format(IdGenerateException.ERROR_CLOCK_BACK, averageTimestamp - timestamp));
            }
            registerNode();
            startHeartBeatThread();
            // 保存workerId到本地文件
            WorkerIdUtils.writePidFile(pidHome + File.separatorChar + pidName + WorkerIdUtils.WORKER_SPLIT + workerId);
        } catch (Exception e) {
            active.set(false);
            if (null != socket) {
                socket.close();
            }
            throw e;
        }
    }

    /**
     * 获取workerId
     * 如果之前已经分配，那么就直接载入之前分配的id值
     * 如果之前未分配，那么就新分配一个id值
     *
     * @return workerId
     */
    private Long loadWorkerId() throws Exception {
        // 先从本地文件读取，这样即使依赖的中间件服务不可用也可以获取workId，降低对中间件的依赖关系
        Long workerId = loadWorkerIdFromLocalFile();
        if (null == workerId) {
            // 从中间件获取
            workerId = loadWorkerIdFromMiddleware();
        }
        if (null == workerId) {
            // 分配一个worker Id
            workerId = assignWorkerIdByMiddleware();
        }
        return workerId;
    }

    /**
     * 从本地文件获取worker id
     *
     * @return
     */
    private Long loadWorkerIdFromLocalFile() {
        return WorkerIdUtils.getWorkerId(pidHome, pidName);
    }

    /**
     * 心跳线程，用于每隔一段时间上报一次临时节点时间
     */
    protected void startHeartBeatThread() {
        ScheduledExecutorService scheduledPool = new ScheduledThreadPoolExecutor(1, new NamingThreadFactory(THREAD_HEARTBEAT_NAME, true, null));
        scheduledPool.scheduleAtFixedRate(() -> {
            if (active.get() == false) {
                scheduledPool.shutdownNow();
            } else if (null != workerId) {
                report();
            }
        }, 0L, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * 初始化环境
     *
     * @throws Exception
     */
    protected abstract void initEnv() throws Exception;

    /**
     * 从中间件获取已分配的worker id
     *
     * @return worker id
     * @throws Exception
     */
    protected abstract Long loadWorkerIdFromMiddleware() throws Exception;

    /**
     * 依靠中间件分配一个worker id
     *
     * @return worker id
     * @throws Exception
     */
    protected abstract Long assignWorkerIdByMiddleware() throws Exception;

    /**
     * 依靠中间件获取上一次记录的时间
     *
     * @return 上一次记录的时间
     * @throws Exception
     */
    protected abstract long lastTime() throws Exception;

    /**
     * 获取机器节点列表的活跃时间平均值
     *
     * @return 机器节点列表的活跃时间平均值
     */
    protected abstract long averageTime();

    /**
     * 注册节点到节点列表中
     *
     * @throws Exception
     */
    protected abstract void registerNode() throws Exception;

    /**
     * 心跳上报
     */
    protected abstract void report();

    @Override
    public long assignWorkerId() {
        return workerId;
    }

}
