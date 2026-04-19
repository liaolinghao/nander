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
package wang.bigbird.domain.framework.server.web.core.support.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.StringUtils;
import wang.bigbird.domain.framework.common.sensitive.base.enums.SensitiveTypeEnum;
import wang.bigbird.domain.framework.common.sensitive.base.util.SensitiveUtils;

import java.io.IOException;

/**
 * 手机号脱敏序列化器
 *
 * @author Bigbird
 */
public class MobilephoneSensitiveSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (StringUtils.isNotBlank(value)) {
            gen.writeString(SensitiveUtils.sensitive(SensitiveTypeEnum.MOBILE_PHONE_NUMBERS, value));
        } else {
            gen.writeNull();
        }
    }

}
