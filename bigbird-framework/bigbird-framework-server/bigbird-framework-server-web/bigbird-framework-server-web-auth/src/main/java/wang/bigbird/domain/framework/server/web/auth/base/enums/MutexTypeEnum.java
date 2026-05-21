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
 * 登录互斥类型
 *
 * @author Bigbird
 */
public enum MutexTypeEnum implements ValuedEnum<Byte> {

    /**
     * 不互斥
     */
    none((byte) 0),
    /**
     * 考虑渠道类别，先登录的有效，拒绝后登录
     * 必须传递了登录设备ID，才能有效使用该模式
     */
    front_channel((byte) 1),
    /**
     * 考虑渠道类别，后登录踢掉先登录
     */
    back_channel((byte) 2),
    /**
     * 不考虑渠道类别，先登录的有效，拒绝后登录
     * 必须传递了登录设备ID，才能有效使用该模式
     */
    front_all((byte) 3),
    /**
     * 不考虑渠道类别，后登录踢掉先登录
     */
    back_all((byte) 4);

    private byte type;

    MutexTypeEnum(byte type) {
        this.type = type;
    }

    /**
     * 获取登录互斥类型枚举对象
     *
     * @param type 登录互斥类型
     * @return 登录互斥类型枚举对象
     */
    public static MutexTypeEnum getInstanceByType(byte type) {
        for (MutexTypeEnum v : MutexTypeEnum.values()) {
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

    /**
     * 是否优先保证先登录有效
     *
     * @return 是否优先保证先登录有效
     */
    public boolean isFrontProtected() {
        return this == MutexTypeEnum.front_channel || this == MutexTypeEnum.front_all;
    }

    /**
     * 是否优先保证后登录有效
     *
     * @return 是否优先保证后登录有效
     */
    public boolean isBackProtected() {
        return this == MutexTypeEnum.back_channel || this == MutexTypeEnum.back_all;
    }

    /**
     * 是否忽略登录渠道
     *
     * @return 是否忽略登录渠道
     */
    public boolean isIgnoreChannel() {
        return this == MutexTypeEnum.front_all || this == MutexTypeEnum.back_all;
    }

}
