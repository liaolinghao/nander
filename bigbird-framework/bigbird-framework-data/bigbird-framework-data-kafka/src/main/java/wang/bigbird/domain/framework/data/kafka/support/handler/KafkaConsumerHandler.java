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
package wang.bigbird.domain.framework.data.kafka.support.handler;

import java.util.List;

/**
 * kafka消费处理器
 *
 * @author Bigbird
 */
@FunctionalInterface
public interface KafkaConsumerHandler<T> {

    /**
     * 消息处理
     *
     * @param msgs 消息集合
     */
    default void handle(List<T> msgs) {
        handle(msgs, null);
    }

    /**
     * 带消费上下文，可判断是否需要停止
     *
     * @param msgs 消息列表
     * @param ctx  消费上下文
     */
    void handle(List<T> msgs, ConsumerContext ctx);

    /**
     * 消费上下文，用于业务判断shutdown信号
     */
    class ConsumerContext {

        private volatile boolean running;

        public boolean isRunning() {
            return running;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }
    }

}
