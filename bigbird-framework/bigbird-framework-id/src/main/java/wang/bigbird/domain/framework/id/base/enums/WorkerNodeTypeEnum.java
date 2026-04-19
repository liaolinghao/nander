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
 * 运行节点类型
 * <p>
 * CONTAINER: Such as Docker
 * ACTUAL: Actual machine
 *
 * @author Bigbird
 */
public enum WorkerNodeTypeEnum implements ValuedEnum<Integer> {

    /**
     * 容器
     */
    CONTAINER(1),
    /**
     * 物理机
     */
    ACTUAL(2);

    private final Integer type;

    /**
     * Constructor with field of type
     */
    WorkerNodeTypeEnum(Integer type) {
        this.type = type;
    }

    @Override
    public Integer value() {
        return type;
    }

}
