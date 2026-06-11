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
import wang.bigbird.domain.framework.core.base.util.DateUtils;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

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
        mapper = new ObjectMapper();
        JsonUtils.registerJavaTimeModule(mapper);
        JsonUtils.configurateObjectMapper(mapper);
        // 开启类型写入
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
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
            if (raw instanceof String) {
                String value = (String) raw;
                if (StringUtils.isBlank(value)) {
                    return raw;
                }
                if (StringUtils.isDateTime(value)) {
                    // 裸字符串 "yyyy-MM-dd HH:mm:ss"，单纯靠 Jackson 自动类型推断，做不到自动转 LocalDateTime
                    // 除非将其包装到POJO中，为此，这里做一次特殊判断，以便适配单纯的LocalDateTime缓存值返回
                    try {
                        return DateUtils.toLocalDateTime(DateUtils.parse(value, DateUtils.STANDARD_PATTERN));
                    } catch (Exception ignored) {

                    }
                } else if (StringUtils.isInteger(value)) {
                    try {
                        long numVal = Long.parseLong(value);
                        if (numVal >= Integer.MIN_VALUE && numVal <= Integer.MAX_VALUE) {
                            return Integer.valueOf((int) numVal);
                        }
                        return Long.valueOf(numVal);
                    } catch (Exception ignored) {
                    }
                } else if (StringUtils.isDecimal(value)) {
                    try {
                        double numVal = Double.parseDouble(value);
                        if (numVal >= -Float.MAX_VALUE && numVal <= Float.MAX_VALUE) {
                            // 此处将double转换为float存在精度丢失风险（＞7 位）
                            return Float.valueOf((float) numVal);
                        }
                        return Double.valueOf(numVal);
                    } catch (Exception ignored) {
                    }
                }
            }
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
