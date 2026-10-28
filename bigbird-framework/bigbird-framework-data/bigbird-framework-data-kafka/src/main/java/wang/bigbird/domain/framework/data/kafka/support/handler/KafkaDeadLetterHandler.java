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

import wang.bigbird.domain.framework.data.kafka.domain.pojo.DeadLetterRecord;
import wang.bigbird.domain.framework.data.kafka.exception.DeadLetterSendException;

import java.util.List;

/**
 * 死信投递器
 *
 * @author Bigbird
 */
public interface KafkaDeadLetterHandler {

    /**
     * 投递死信
     *
     * @param record 死信
     * @throws DeadLetterSendException
     */
    void send(DeadLetterRecord record) throws DeadLetterSendException;

    /**
     * 批量投递，默认逐条 send。
     * 逐条投递可避免某一条失败导致整批重复投递。
     *
     * @param records 死信集合
     * @throws DeadLetterSendException
     */
    default void sendBatch(List<DeadLetterRecord> records) throws DeadLetterSendException {
        for (DeadLetterRecord record : records) {
            send(record);
        }
    }

}
