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
package wang.bigbird.domain.framework.data.kafka.domain.pojo;

import lombok.Builder;
import lombok.Data;
import org.apache.kafka.common.header.Headers;

/**
 * 死信消息记录
 *
 * @author Bigbird
 */
@Data
@Builder
public class DeadLetterRecord<T> {

    /**
     * 消费组
     */
    private String groupId;
    /**
     * 原始 topic
     */
    private String originalTopic;
    /**
     * 分区
     */
    private int partition;
    /**
     * 偏移值
     */
    private long offset;
    /**
     * 原始消息 key
     */
    private String key;
    /**
     * 原始消息 value，不做任何转换
     */
    private byte[] value;
    /**
     * 原始消息 headers
     */
    private Headers headers;
    /**
     * 消息类型
     */
    private Class<T> messageClass;
    /**
     * 异常摘要
     */
    private String errorMessage;
    /**
     * 进入死信的时间戳
     */
    private long sendTimestamp;

}
