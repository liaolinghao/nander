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
package wang.bigbird.domain.framework.id.base.enums;

import wang.bigbird.domain.framework.core.base.util.enums.ValuedEnum;

/**
 * WorkerID生成策略
 *
 * @author Bigbird
 */
public enum WorkerIdStrategyEnum implements ValuedEnum<String> {

    /**
     * 固定值为0
     */
    zero("zero"),
    /**
     * 利用数据库来实现workerId的提供管理
     */
    db("db"),
    /**
     * 利用redis来实现workerId的提供管理
     */
    redis("redis"),
    /**
     * 利用zookeeper来实现workerId的提供管理
     */
    zk("zk");

    private final String name;

    WorkerIdStrategyEnum(String name) {
        this.name = name;
    }

    @Override
    public String value() {
        return name;
    }

}
