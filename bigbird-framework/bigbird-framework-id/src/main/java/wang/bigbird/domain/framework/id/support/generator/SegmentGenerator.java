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
package wang.bigbird.domain.framework.id.support.generator;

import wang.bigbird.domain.framework.id.base.enums.IdStrategyEnum;
import wang.bigbird.domain.framework.id.support.strategy.meituan.leaf.segment.SegmentIdWorker;

/**
 * 基于leaf-segment策略的ID生成器
 *
 * @author Bigbird
 */
public class SegmentGenerator implements IdGenerator {

    private final static String MSG_UID_PARSE = "{\"UID\":\"%s\"}";

    private SegmentIdWorker segmentIdWorker;

    public SegmentGenerator(SegmentIdWorker segmentIdWorker){
        this.segmentIdWorker = segmentIdWorker;
    }

    @Override
    public IdStrategyEnum getStrategy() {
        return IdStrategyEnum.segment;
    }

    @Override
    public long getUid(String bizTag) {
        return segmentIdWorker.get(bizTag);
    }

    @Override
    public String parseUid(long uid, String bizTag) {
        return String.format(MSG_UID_PARSE, uid);
    }

}
