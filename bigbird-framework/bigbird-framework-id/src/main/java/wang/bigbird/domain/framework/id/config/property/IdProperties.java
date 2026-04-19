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
package wang.bigbird.domain.framework.id.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import wang.bigbird.domain.framework.id.base.enums.IdStrategyEnum;
import wang.bigbird.domain.framework.id.base.enums.WorkerIdStrategyEnum;

/**
 * ID 属性
 *
 * @author Bigbird
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "bigbird.id")
public class IdProperties {

    /**
     * ID生成策略
     */
    private IdStrategyEnum strategy = IdStrategyEnum.snowflake;

    /**
     * 基因因子，建议采用一个比较大的数值
     */
    private Long factor;

    /**
     * 是否启用基因编码
     */
    private boolean enableGeneCoding = true;

    /**
     * worker id生成策略
     */
    private final WorkerId workerId = new WorkerId();

    private final Twitter twitter = new Twitter();

    private final Baidu baidu = new Baidu();

    @Data
    public static class WorkerId {
        /**
         * WorkerID生成策略
         */
        private WorkerIdStrategyEnum strategy = WorkerIdStrategyEnum.zero;
        /**
         * 心跳间隔时间
         */
        private Long interval = 3000L;
        /**
         * 本地workerID文件存储根路径
         */
        private String pidHome = "/data/pids/";

        /**
         * 使用端口（同机多uid应用时区分端口）
         * 当一台机器上只部署一个id服务时，该端口值可以不指定，采用默认值-1
         * 当一台机器上部署多个id服务时，需要对每个id服务分别指定端口
         */
        private Integer pidPort = -1;
    }

    /**
     * Twitter Snowflake配置
     */
    @Data
    public static class Twitter {
        /**
         * 工作机器ID（0~31）
         */
        private Long workerId;

        /**
         * 数据中心ID（0~31）
         */
        private Long datacenterId;
    }

    /**
     * Baidu Uid 配置
     */
    @Data
    public static class Baidu {
        /**
         * 时间戳部分长度
         */
        private Integer timeBits = 28;

        /**
         * 机器ID部分长度
         */
        private Integer workerBits = 22;

        /**
         * 序列号部分长度
         */
        private Integer seqBits = 13;

        /**
         * 起始日期
         */
        private String epochStr = "2016-05-20";

        /**
         * RingBuffer size扩容参数，可提高UID生成的吞吐量，
         * 默认:3，原bufferSize=8192，扩容后bufferSize= 8192 << 3 = 65536
         */
        private Integer boostPower = 3;

        /**
         * 指定何时向RingBuffer中填充UID，取值为百分比(0, 100)，默认为50
         * 举例：bufferSize=1024，paddingFactor=50 -> threshold=1024 * 50 / 100 = 512
         * 当环上可用UID数量 < 512时，将自动对RingBuffer进行填充补全
         */
        private Integer paddingFactor = 50;

        /**
         * 另外一种RingBuffer填充时机，在Schedule线程中周期性检查填充
         * 默认：不配置此项，即不实用Schedule线程。如需使用，请指定Schedule线程时间间隔，单位：秒
         */
        private Long scheduleInterval;

    }

}
