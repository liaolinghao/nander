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
package wang.bigbird.domain.framework.server.web.core.base.enums;

import wang.bigbird.domain.framework.core.base.util.enums.ValuedEnum;

/**
 * 设备屏幕类型
 *
 * @author Bigbird
 */
public enum DeviceScreenTypeEnum implements ValuedEnum<String> {

    STD("std", "标准屏"),
    SMALL("small", "小屏"),
    UNSPECIFIED("unspecified", "未定义类型"),
    UNKNOW("unknow", "未知类型");

    private String code;
    private String desc;

    DeviceScreenTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取设备屏幕类型枚举对象
     *
     * @param code 设备屏幕类型代码
     * @return 设备屏幕类型枚举对象
     */
    public static DeviceScreenTypeEnum getInstanceByCode(String code) {
        for (DeviceScreenTypeEnum dst : DeviceScreenTypeEnum.values()) {
            if (dst.code.equalsIgnoreCase(code)) {
                return dst;
            }
        }
        return UNKNOW;
    }

    @Override
    public String value() {
        return code;
    }

    /**
     * 作为RPC接口参数，序列化需要利用该方法
     *
     * @return
     */
    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
