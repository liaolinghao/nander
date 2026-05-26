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
package wang.bigbird.domain.framework.data.redis.support.handler;

/**
 * redis消息订阅处理器
 *
 * @author Bigbird
 */
@FunctionalInterface
public interface RedisSubscriberHandler {

    /**
     * 渠道订阅消息处理
     *
     * @param pattern 渠道模式
     * @param channel 渠道
     * @param msg     消息
     */
    void handle(CharSequence pattern, CharSequence channel, String msg);

}
