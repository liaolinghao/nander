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
package wang.bigbird.domain.framework.core.base.tool;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 自定义的本地时间序列化器，作用在于当时间值等于
 * 自定义设置的null时间模式时，序列化不返回内容
 *
 * @author Bigbird
 */
public class CustomLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    /**
     * 格式
     */
    private DateTimeFormatter dateTimeFormatter;

    /**
     * 设置为 null 的时间表达式
     */
    private String setNullDateTime;

    public CustomLocalDateTimeSerializer(DateTimeFormatter dateTimeFormatter, String setNullDateTime) {
        this.dateTimeFormatter = dateTimeFormatter;
        this.setNullDateTime = setNullDateTime;
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator g, SerializerProvider provider)
            throws IOException {
        String format = value.format(dateTimeFormatter);
        if (StringUtils.isNotBlank(setNullDateTime)
                && !CommonConstants.NULL.equals(setNullDateTime)
                && format.equals(setNullDateTime)) {
            // 当设置了setNullDateTime，且值不为null，同时当前时间格式串等于setNullDateTime不
            // 返回内容，比如初始时间：1970-01-01 00:00:00，可以认为设置的时间为null
            g.writeNull();
        } else {
            g.writeString(format);
        }
    }

}
