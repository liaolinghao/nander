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
package wang.bigbird.domain.framework.cache.support.redission;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;

import java.nio.charset.StandardCharsets;


/**
 * 自定义 JsonJacksonCodec
 *
 * @author Bigbird
 */
public class CustomizedJsonJacksonCodec implements Codec {

    public static final CustomizedJsonJacksonCodec INSTANCE = new CustomizedJsonJacksonCodec();

    /**
     * Key沿用标准字符串编解码
     */
    private static final StringCodec STRING_CODEC = StringCodec.INSTANCE;

    private final ObjectMapper mapper;
    private final Encoder valueEncoder;
    private final Decoder<Object> valueDecoder;
    private final Encoder mapValueEncoder;
    private final Decoder<Object> mapValueDecoder;

    private CustomizedJsonJacksonCodec() {
        mapper = JsonUtils.getRegisterMapper();
        // 开启类型写入
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                // 所有对象，包含 final 包装类 Long/Integer/Float 都会带上 @class
                // 解决缓存反序列化时数据类型问题
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );

        // 值编码器
        valueEncoder = obj -> {
            ByteBuf buffer = Unpooled.buffer();
            try {
                String json = mapper.writeValueAsString(obj);
                buffer.writeCharSequence(json, StandardCharsets.UTF_8);
                return buffer;
            } catch (Exception e) {
                buffer.release();
                throw e;
            }
        };

        // 关键：TypeReference<> 保留类型信息，LocalDateTime可还原
        valueDecoder = (buf, state) -> {
            String json = buf.toString(StandardCharsets.UTF_8);
            Object raw = mapper.readValue(json, new TypeReference<>() {
            });
            return raw;
        };

        // RMap 的 value 复用同一套序列化
        mapValueEncoder = valueEncoder;
        mapValueDecoder = valueDecoder;
    }

    @Override
    public Decoder<Object> getMapValueDecoder() {
        return mapValueDecoder;
    }

    @Override
    public Encoder getMapValueEncoder() {
        return mapValueEncoder;
    }

    @Override
    public Decoder<Object> getMapKeyDecoder() {
        return STRING_CODEC.getMapKeyDecoder();
    }

    @Override
    public Encoder getMapKeyEncoder() {
        return STRING_CODEC.getMapKeyEncoder();
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return valueDecoder;
    }

    @Override
    public Encoder getValueEncoder() {
        return valueEncoder;
    }

    @Override
    public ClassLoader getClassLoader() {
        return CustomizedJsonJacksonCodec.class.getClassLoader();
    }

}
