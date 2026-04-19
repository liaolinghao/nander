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
package wang.bigbird.domain.framework.data.rabbitmq.support.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import wang.bigbird.domain.framework.core.base.tool.Coder;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提供消息处理的公共方法
 *
 * @author Bigbird
 */
@Slf4j
public class MessageHandler {

    /**
     * 队列消息数据对应的类型
     */
    public static Map<String, Class> queueMessageClassMap = new HashMap<>();

    /**
     * 队列消息数据对应的处理器
     */
    public static Map<String, RabbitmqConsumerHandler> queueMessageConsumerHandlerMap = new HashMap<>();

    private static ObjectMapper objectMapper = JsonUtils.getMapper();

    /**
     * 拆解消息进行处理
     *
     * @param message
     * @param messageClass
     * @param handler
     * @param <T>
     * @throws IOException
     */
    public static <T> void parseMessage(Message message, Class<T> messageClass, RabbitmqConsumerHandler<T> handler) throws
            IOException {
        String body = new String(message.getBody(), Coder.DEFAULT_ENCODING);
        JsonNode payload = JsonUtils.json2Node(body).get("payload");
        String typeId = message.getMessageProperties().getHeader("__TypeId__");
        switch (typeId) {
            case "org.springframework.messaging.support.GenericMessage":
                doHandle(messageClass, handler, objectMapper.writeValueAsBytes(payload));
                break;
            case "org.springframework.messaging.support.ErrorMessage":
                log.error(payload.toString());
                break;
            default:
                break;
        }
    }

    /**
     * 消息处理
     *
     * @param messageClass
     * @param handler
     * @param value
     * @param <T>
     */
    private static <T> void doHandle(Class<T> messageClass, RabbitmqConsumerHandler<T> handler, byte[] value) throws
            IOException {
        handler.handle(List.of(objectMapper.readValue(value, messageClass)));
    }

}
