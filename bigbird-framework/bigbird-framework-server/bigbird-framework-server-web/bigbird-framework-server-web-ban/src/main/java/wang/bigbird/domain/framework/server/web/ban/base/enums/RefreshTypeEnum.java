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
package wang.bigbird.domain.framework.server.web.ban.base.enums;

import wang.bigbird.domain.framework.core.base.util.enums.ValuedEnum;

/**
 * 禁用词刷新事件类型
 *
 * @author Bigbird
 */
public enum RefreshTypeEnum implements ValuedEnum<String> {

    /**
     * 添加禁用词
     */
    add("ADD"),
    /**
     * 删除禁用词
     */
    delete("DELETE");

    private String type;

    RefreshTypeEnum(String type) {
        this.type = type;
    }

    /**
     * 获取禁用词刷新事件类型枚举对象
     *
     * @param type 禁用词刷新事件类型代码
     * @return 禁用词刷新事件类型枚举对象
     */
    public static RefreshTypeEnum getInstanceByType(String type) {
        for (RefreshTypeEnum rt : RefreshTypeEnum.values()) {
            if (rt.value().equals(type)) {
                return rt;
            }
        }
        return null;
    }

    @Override
    public String value() {
        return type;
    }

    /**
     * 作为RPC接口参数，序列化需要利用该方法
     *
     * @return
     */
    public String getType() {
        return type;
    }

}
