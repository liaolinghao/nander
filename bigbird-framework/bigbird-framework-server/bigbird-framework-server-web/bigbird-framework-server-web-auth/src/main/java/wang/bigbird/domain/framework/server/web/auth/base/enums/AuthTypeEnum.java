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
package wang.bigbird.domain.framework.server.web.auth.base.enums;

import wang.bigbird.domain.framework.core.base.util.enums.ValuedEnum;

/**
 * 认证机制类型
 *
 * @author Bigbird
 */
public enum AuthTypeEnum implements ValuedEnum<Byte> {

    /**
     * 标准双token机制
     */
    token((byte) 0),
    /**
     * 仿Session机制
     */
    session((byte) 1);

    private byte type;

    AuthTypeEnum(byte type) {
        this.type = type;
    }

    /**
     * 获取认证机制类型枚举对象
     *
     * @param type 认证机制类型
     * @return 认证机制类型枚举对象
     */
    public static AuthTypeEnum getInstanceByType(byte type) {
        for (AuthTypeEnum v : AuthTypeEnum.values()) {
            if (v.value() == type) {
                return v;
            }
        }
        return null;
    }

    @Override
    public Byte value() {
        return type;
    }

    /**
     * 作为RPC接口参数，序列化需要利用该方法
     *
     * @return
     */
    public Byte getType() {
        return type;
    }

}
