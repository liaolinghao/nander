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
package wang.bigbird.domain.framework.common.crypto.base.util;

import lombok.NonNull;

import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 签名处理工具
 *
 * @author Bigbird
 */
public class SignUtils {

    SortedMap<String, Object> map = new TreeMap<>();

    /**
     * 增加字段值
     *
     * @param field 字段
     * @param value 值，如果是null，则使用""
     * @return
     */
    public SignUtils add(@NonNull String field, Object value) {
        map.put(field, Objects.toString(value, ""));
        return this;
    }

    /**
     * 返回按照编码排序的字符串
     *
     * @return 例如：a=1&d=&last=3
     */
    public String toText() {
        return map.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
    }
}
