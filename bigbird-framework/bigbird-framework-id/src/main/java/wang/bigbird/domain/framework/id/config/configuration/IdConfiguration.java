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
package wang.bigbird.domain.framework.id.config.configuration;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import wang.bigbird.domain.framework.id.config.property.IdProperties;
import wang.bigbird.domain.framework.id.support.IdGenerator;
import wang.bigbird.domain.framework.id.support.SegmentGenerator;
import wang.bigbird.domain.framework.id.support.SnowflakeGenerator;
import wang.bigbird.domain.framework.id.support.UidGenerator;
import wang.bigbird.domain.framework.id.support.assigner.*;
import wang.bigbird.domain.framework.id.support.creator.IdCreator;
import wang.bigbird.domain.framework.id.support.strategy.meituan.leaf.segment.SegmentIdWorker;

import javax.annotation.PostConstruct;

/**
 * ID配置
 *
 * @author Bigbird
 */
@Slf4j
@Configuration
@ComponentScan("wang.bigbird.domain.framework.id")
@MapperScan("wang.bigbird.domain.framework.id.dao")
public class IdConfiguration {

    @PostConstruct
    public void init() {
        log.info("Init id framework.");
    }

    @Bean
    public WorkerIdAssigner workerIdAssigner(IdProperties idProperties) {
        switch (idProperties.getWorkerId().getStrategy()) {
            case db:
                return new DisposableWorkerIdAssigner();
            case redis:
                return new RedisWorkerIdAssigner(idProperties.getWorkerId().getInterval(), idProperties.getWorkerId().getPidHome(), idProperties.getWorkerId().getPidPort());
            case zk:
                return new ZkWorkerIdAssigner(idProperties.getWorkerId().getInterval(), idProperties.getWorkerId().getPidHome(), idProperties.getWorkerId().getPidPort());
            default:
                return new ZeroWorkerIdAssigner();
        }
    }

    @Bean
    public IdGenerator idGenerator(WorkerIdAssigner workerIdAssigner, IdProperties idProperties) {
        switch (idProperties.getStrategy()) {
            case segment:
                return new SegmentGenerator(new SegmentIdWorker());
            case uid:
                return new UidGenerator(idProperties.getBaidu().getTimeBits(), idProperties.getBaidu().getWorkerBits(), idProperties.getBaidu().getSeqBits(), idProperties.getBaidu().getEpochStr(), workerIdAssigner, idProperties.getBaidu().getBoostPower(), idProperties.getBaidu().getPaddingFactor(), idProperties.getBaidu().getScheduleInterval(), null, null);
            case snowflake:
            default:
                return new SnowflakeGenerator(idProperties.getTwitter().getWorkerId(), idProperties.getTwitter().getDatacenterId(), workerIdAssigner);
        }
    }

    @Bean
    public IdCreator idCreator(IdGenerator idGenerator, IdProperties idProperties) {
        return new IdCreator(idGenerator, idProperties.getFactor(), idProperties.isEnableGeneCoding());
    }

}
