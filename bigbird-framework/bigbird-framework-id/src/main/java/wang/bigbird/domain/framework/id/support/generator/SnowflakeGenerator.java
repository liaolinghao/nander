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
package wang.bigbird.domain.framework.id.support;

import wang.bigbird.domain.framework.id.base.enums.IdStrategyEnum;
import wang.bigbird.domain.framework.id.support.assigner.WorkerIdAssigner;
import wang.bigbird.domain.framework.id.support.strategy.twitter.SnowflakeIdWorker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于twitter-snowflake策略的ID生成器
 *
 * @author Bigbird
 */
public class SnowflakeGenerator implements IdGenerator {

    /**
     * 生成器集合
     */
    private static Map<String, SnowflakeIdWorker> generatorMap = new ConcurrentHashMap<>();

    /**
     * 机器ID
     */
    private Long workerId;

    /**
     * 数据中心id
     */
    private Long datacenterId;

    /**
     * worker id 分配器
     */
    private WorkerIdAssigner workerIdAssigner;

    public SnowflakeGenerator(Long workerId, Long datacenterId, WorkerIdAssigner workerIdAssigner) {
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.workerIdAssigner = workerIdAssigner;
    }

    @Override
    public IdStrategyEnum getStrategy() {
        return IdStrategyEnum.snowflake;
    }

    @Override
    public long getUid(String bizTag) {
        return getSnowflakeIdWorker(bizTag).nextId();
    }

    @Override
    public String parseUid(long uid, String bizTag) {
        return getSnowflakeIdWorker(bizTag).parseUid(uid);
    }

    private SnowflakeIdWorker getSnowflakeIdWorker(String bizTag) {
        SnowflakeIdWorker snowflakeIdWorker = generatorMap.get(bizTag);
        if (null == snowflakeIdWorker) {
            synchronized (generatorMap) {
                snowflakeIdWorker = generatorMap.get(bizTag);
                if (null == snowflakeIdWorker) {
                    // 机器id--默认取进程id
                    long realWid = 0;
                    if (null != workerId) {
                        realWid = workerId;
                    } else if (null != workerIdAssigner) {
                        realWid = workerIdAssigner.assignWorkerId();
                    }
                    // 数据中心id--默认取机器码
                    Long realDid = null == datacenterId ? 0 : datacenterId;
                    snowflakeIdWorker = new SnowflakeIdWorker(realWid, realDid);
                    generatorMap.put(bizTag, snowflakeIdWorker);
                }
            }
        }
        return snowflakeIdWorker;
    }
}
