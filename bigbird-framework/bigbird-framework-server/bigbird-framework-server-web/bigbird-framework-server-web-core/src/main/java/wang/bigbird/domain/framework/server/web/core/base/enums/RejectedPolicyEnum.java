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
package wang.bigbird.domain.framework.server.web.core.base.enums;

/**
 * 线程池拒绝策略类型
 *
 * @author Bigbird
 */
public enum RejectedPolicyEnum {

    /**
     * 默认策略
     * <p>
     * 直接抛出RejectedExecutionException异常
     * 这种策略适合任务必须执行且不能丢弃的场景
     * 调用方可以捕获异常并做相应处理
     * 示例场景：关键业务处理，订单处理等
     */
    ABORT,

    /**
     * 调用者执行策略
     * <p>
     * 在调用者线程中直接执行被拒绝的任务
     * 不会丢弃任务，也不会抛出异常
     * 会降低任务提交的速度（因为调用者要自己执行任务）
     * 示例场景：对响应时间不敏感的批量处理任务
     */
    CALLER_RUNS,

    /**
     * 直接丢弃策略
     * <p>
     * 直接丢弃被拒绝的任务，不做任何处理
     * 不会抛出异常
     * 适合任务可以被丢弃的场景
     * 示例场景：日志记录、监控数据收集等
     */
    DISCARD,

    /**
     * 最旧丢弃策略
     * <p>
     * 丢弃队列头部（最旧）的任务，然后重试执行当前任务
     * 不会抛出异常
     * 适合只关心最新任务的场景
     * 示例场景：实时数据处理，只关心最新状态的更新
     */
    DISCARD_OLDEST

}
