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
package wang.bigbird.domain.framework.id.support.strategy.meituan.leaf.segment;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 号段，可容纳指定数量的号码
 *
 * @author Bigbird
 */
public class SegmentBO {

    /**
     * 当前分发ID值
     */
    @Getter
    @Setter
    private AtomicLong value = new AtomicLong(0);
    /**
     * 当前号段最大值
     */
    @Getter
    @Setter
    private volatile long max;
    /**
     * 每次分配的号段数量
     */
    @Getter
    @Setter
    private volatile int step;
    /**
     * 缓存号段
     */
    @Getter
    private SegmentBufferBO buffer;

    public SegmentBO(SegmentBufferBO buffer) {
        this.buffer = buffer;
    }

    /**
     * 可用号段数量
     *
     * @return
     */
    public long getIdle() {
        return this.getMax() - getValue().get();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Segment(");
        sb.append("value:");
        sb.append(value);
        sb.append(",max:");
        sb.append(max);
        sb.append(",step:");
        sb.append(step);
        sb.append(")");
        return sb.toString();
    }
}
