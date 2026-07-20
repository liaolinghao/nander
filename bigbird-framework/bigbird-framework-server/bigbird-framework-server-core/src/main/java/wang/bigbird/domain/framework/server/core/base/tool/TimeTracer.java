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
package wang.bigbird.domain.framework.server.core.base.tool;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import wang.bigbird.domain.framework.server.core.support.holder.SpringContextHolder;

import java.util.function.Supplier;

/**
 * 时间追踪器
 *
 * @author Bigbird
 */
@Data
@Slf4j
public class TimeTracer {

    /**
     * 默认开启开关配置key
     */
    public static final String DEFAULT_SWITCH_KEY = "bigbird.server.core.tracer.enable";
    /**
     * 默认慢阈值配置key
     */
    public static final String DEFAULT_THRESHOLD_KEY = "bigbird.server.core.tracer.threshold.ms";
    /**
     * 默认慢阀值时间
     */
    private static final long DEFAULT_SLOW_THRESHOLD = 300;

    private final StopWatch stopWatch;
    private final boolean traceEnable;
    private final long slowThreshold;

    private final String moduleTag;
    private final String switchKey;
    private final String thresholdKey;

    /**
     * 当前正在执行的任务名称
     */
    private String runningTaskName;

    private TimeTracer(String moduleTag, String switchKey, String thresholdKey) {
        this.moduleTag = moduleTag;
        this.switchKey = switchKey;
        this.thresholdKey = thresholdKey;

        // 动态读取配置，实时生效
        Boolean enableCfg = SpringContextHolder.getConfigValue(switchKey, Boolean.class);
        this.traceEnable = enableCfg == null || Boolean.TRUE.equals(enableCfg);

        Long thresholdCfg = SpringContextHolder.getConfigValue(thresholdKey, Long.class);
        this.slowThreshold = thresholdCfg != null ? thresholdCfg : DEFAULT_SLOW_THRESHOLD;

        this.stopWatch = new StopWatch();
    }

    /**
     * 使用默认配置key创建实例
     *
     * @param moduleTag 模块标识，日志区分业务，如 contact、order、user
     */
    public static TimeTracer create(String moduleTag) {
        return new TimeTracer(moduleTag, DEFAULT_SWITCH_KEY, DEFAULT_THRESHOLD_KEY);
    }

    /**
     * 自定义配置key创建实例（不同业务使用独立开关）
     *
     * @param moduleTag    模块标识
     * @param switchKey    开启开关配置key
     * @param thresholdKey 慢阈值配置key
     */
    public static TimeTracer create(String moduleTag, String switchKey, String thresholdKey) {
        return new TimeTracer(moduleTag, switchKey, thresholdKey);
    }

    public void start(String taskName) {
        if (!traceEnable) {
            return;
        }
        // 安全校验：存在未停止任务
        if (stopWatch.isRunning()) {
            log.error("[TimeTracer][{}] Exception: Task[{}] has not been stopped, force termination will be performed, new task[{}] will start shortly",
                    moduleTag, runningTaskName, taskName);
            stopWatch.stop();
            runningTaskName = null;
        }
        stopWatch.start(taskName);
        runningTaskName = taskName;
    }

    public void stop() {
        if (!traceEnable) {
            return;
        }
        if (stopWatch.isRunning()) {
            stopWatch.stop();
            runningTaskName = null;
        } else {
            log.warn("[TimeTracer][{}] Warning: No running task, duplicate stop call", moduleTag);
        }
    }

    /**
     * 同步执行任务，同时记录耗时，自动闭环时间追踪器
     *
     * @param taskName 任务名称
     * @param action   任务体
     */
    public void task(String taskName, Runnable action) {
        if (!traceEnable) {
            action.run();
            return;
        }
        start(taskName);
        try {
            action.run();
        } finally {
            stop();
        }
    }

    /**
     * 支持有返回值的任务
     * 同步执行任务，同时记录耗时，自动闭环时间追踪器
     *
     * @param taskName 任务名称
     * @param action   任务体
     * @param <T>
     * @return 任务返回值
     */
    public <T> T task(String taskName, Supplier<T> action) {
        if (!traceEnable) {
            return action.get();
        }
        start(taskName);
        try {
            return action.get();
        } finally {
            stop();
        }
    }

    /**
     * 结束并打印耗时日志
     *
     * @param extArgs 附加业务参数 格式 k1,v1,k2,v2...
     */
    public void finishAndPrint(Object... extArgs) {
        if (!traceEnable) {
            return;
        }
        // 兜底：防止最后一段漏stop
        if (stopWatch.isRunning()) {
            log.error("[TimeTracer][{}] Fallback capture: Unstopped task[{}] exists, force stopping", moduleTag, runningTaskName);
            stopWatch.stop();
            runningTaskName = null;
        }
        long totalMs = stopWatch.getTotalTimeMillis();
        StringBuilder extSb = new StringBuilder();
        for (Object arg : extArgs) {
            extSb.append(arg).append(" ");
        }
        String extInfo = extSb.toString().trim();

        String baseLog = "[TimeTracer][{}] totalMs:{} | {}";
        if (totalMs >= slowThreshold) {
            log.warn(baseLog + "\nDetail:\n{}", moduleTag, totalMs, extInfo, stopWatch.prettyPrint());
        } else {
            log.debug(baseLog, moduleTag, totalMs, extInfo);
        }
    }

    /**
     * 获取总耗时毫秒（供监控指标上报使用）
     */
    public long getTotalTimeMs() {
        return stopWatch.getTotalTimeMillis();
    }

    /**
     * 判断计时器是否开启
     *
     * @return 是否开启计时器
     */
    public boolean isEnable() {
        return traceEnable;
    }

}
