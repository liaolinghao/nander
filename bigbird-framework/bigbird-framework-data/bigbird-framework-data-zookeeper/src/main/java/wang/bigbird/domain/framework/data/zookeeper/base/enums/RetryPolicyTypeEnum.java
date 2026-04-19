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
package wang.bigbird.domain.framework.data.zookeeper.base.enums;

/**
 * 重连策略
 *
 * @author Bigbird
 */
public enum RetryPolicyTypeEnum {

    /**
     * 只重连1次
     */
    OneTime,
    /**
     * 重连多次
     */
    NTimes,
    /**
     * 总等待时间超过retryUntilElapsed秒后停止重连
     */
    UntilElapsed,
    /**
     * 以指数级延迟的重连模式
     */
    ExponentialBackoff,
    /**
     * 同ExponentialBackoff，增加了最大重试时间的控制，以防止无限重试。
     */
    BoundedExponentialBackoff;

}
