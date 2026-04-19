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
package wang.bigbird.domain.framework.core.base.util.enums;

import wang.bigbird.domain.framework.core.base.tool.Assert;

/**
 * 值枚举操作工具类
 *
 * @author Bigbird
 */
public class EnumUtils {

    /**
     * 禁止实例化
     */
    private EnumUtils() {
    }

    /**
     * 指定值对应的枚举
     *
     * @param clz   枚举类
     * @param value 指定值
     * @param <T>
     * @param <V>
     * @return 指定值对应的枚举
     */
    public static <T extends ValuedEnum<V>, V> T parse(Class<T> clz, V value) {
        Assert.notNull(clz, "clz can not be null");
        if (value == null) {
            return null;
        }
        for (T t : clz.getEnumConstants()) {
            if (value.equals(t.value())) {
                return t;
            }
        }
        return null;
    }

    /**
     * 获取指定名称对应的枚举
     *
     * @param enumType 枚举类
     * @param name     指定名称
     * @param <T>
     * @return 指定名称对应的枚举
     */
    public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
        if (name == null) {
            return null;
        }
        return Enum.valueOf(enumType, name);
    }

}
