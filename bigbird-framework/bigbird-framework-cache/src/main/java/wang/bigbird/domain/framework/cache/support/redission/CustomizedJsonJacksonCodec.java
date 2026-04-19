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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.codec.JsonJacksonCodec;
import wang.bigbird.domain.framework.core.base.util.DateUtils;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;


/**
 * 自定义 JsonJacksonCodec
 *
 * @author Bigbird
 */
public class CustomizedJsonJacksonCodec extends JsonJacksonCodec {

    public static final JsonJacksonCodec INSTANCE = new CustomizedJsonJacksonCodec();

    private static final String STANDARD_PATTERN = DateUtils.STANDARD_PATTERN;
    private static final String DATE_PATTERN = DateUtils.DATE_PATTERN;
    private static final String TIME_PATTERN = DateUtils.TIME_PATTERN;

    @Override
    protected void init(ObjectMapper objectMapper) {
        super.init(objectMapper);
        JsonUtils.registerJavaTimeModule(objectMapper, STANDARD_PATTERN, DATE_PATTERN, TIME_PATTERN, null);
    }

}
