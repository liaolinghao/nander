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
package wang.bigbird.domain.framework.server.web.defence.base.enums;

/**
 * 应用类型
 *
 * @author Bigbird
 */
public enum AppTypeEnum {

    /**
     * 外部客户服务平台
     */
    customer((byte) 0),
    /**
     * 内部管理平台
     */
    manager((byte) 1),
    /**
     * 外部应用平台
     */
    access((byte) 2);

    private byte idx;

    AppTypeEnum(byte idx) {
        this.idx = idx;
    }

    public byte idx() {
        return idx;
    }

}
