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
package wang.bigbird.domain.framework.server.web.frequency.service.base;

import wang.bigbird.domain.framework.server.web.frequency.exception.*;

/**
 * 频率检查器
 *
 * @author Bigbird
 */
public interface IFrequencyCheckerService {

    /**
     * 每天，某个key，在某个场景下，被调用的次数检查，超过最大次数时抛出异常
     *
     * @param key       某个主体标识
     * @param scene     某个场景，比如注册，登录
     * @param maxCounts 一天内允许的最大次数
     * @throws ExceedMaxTimesOneDayException
     */
    void dayCheck(String key, String scene, int maxCounts)
            throws ExceedMaxTimesOneDayException;

    /**
     * 每天，某个key，在某个场景下，被调用的次数检查，超过最大次数时抛出异常
     *
     * @param key       某个主体标识
     * @param scene     某个场景，比如注册，登录
     * @param maxCounts 一天内允许的最大次数
     * @param increment 每调用一次，计数的增量，默认是1
     * @throws ExceedMaxTimesOneDayException
     */
    void dayCheck(String key, String scene, int maxCounts, int increment)
            throws ExceedMaxTimesOneDayException;

    /**
     * 每小时，某个key，在某个场景下，被调用的次数检查，超过最大次数时抛出异常
     *
     * @param key       某个主体标识
     * @param scene     某个场景，比如注册，登录
     * @param maxCounts 一小时内允许的最大次数
     * @throws ExceedMaxTimesOneHourException
     */
    void hourCheck(String key, String scene, int maxCounts)
            throws ExceedMaxTimesOneHourException;

    /**
     * 每小时，某个key，在某个场景下，被调用的次数检查，超过最大次数时抛出异常
     *
     * @param key       某个主体标识
     * @param scene     某个场景，比如注册，登录
     * @param maxCounts 一小时内允许的最大次数
     * @param increment 每调用一次，计数的增量，默认是1
     * @throws ExceedMaxTimesOneHourException
     */
    void hourCheck(String key, String scene, int maxCounts, int increment)
            throws ExceedMaxTimesOneHourException;

    /**
     * 每分钟，某个key，在某个场景下，被调用的次数检查，超过最大次数时抛出异常
     *
     * @param key       某个主体标识
     * @param scene     某个场景，比如注册，登录
     * @param maxCounts 一分钟内允许的最大调用次数
     * @throws ExceedMaxTimesOneMinuteException
     */
    void minuteCheck(String key, String scene, int maxCounts)
            throws ExceedMaxTimesOneMinuteException;

    /**
     * 每分钟，某个key，在某个场景下，被调用的次数检查，超过最大次数时抛出异常
     *
     * @param key       某个主体标识
     * @param scene     某个场景，比如注册，登录
     * @param maxCounts 一分钟内允许的最大调用次数
     * @param increment 每调用一次，计数的增量，默认是1
     * @throws ExceedMaxTimesOneMinuteException
     */
    void minuteCheck(String key, String scene, int maxCounts, int increment)
            throws ExceedMaxTimesOneMinuteException;

    /**
     * 当天，某个key，在某个场景下，被调用的次数检查，超过最大次数时抛出异常
     *
     * @param key       某个主体标识
     * @param scene     某个场景，比如注册，登录
     * @param maxCounts 当天内允许的最大次数
     * @throws ExceedMaxTimesSameDayException
     */
    void sameDayCheck(String key, String scene, int maxCounts)
            throws ExceedMaxTimesSameDayException;

    /**
     * 当天，某个key，在某个场景下，被调用的次数检查，超过最大次数时抛出异常
     *
     * @param key       某个主体标识
     * @param scene     某个场景，比如注册，登录
     * @param maxCounts 一分钟内允许的最大调用次数
     * @param increment 每调用一次，计数的增量，默认是1
     * @throws ExceedMaxTimesOneMinuteException
     */
    void sameDayCheck(String key, String scene, int maxCounts, int increment)
            throws ExceedMaxTimesSameDayException;

    /**
     * 指定时间内，某个key，在某个场景下，被调用的次数检查，超过最大次数时抛出异常
     *
     * @param key       某个主体标识
     * @param scene     某个场景，比如注册，登录
     * @param maxCounts 指定时间内允许的最大调用次数
     * @param time      指定时间，秒为单位
     * @throws FrequencyRuntimeException
     */
    void timeCheck(String key, String scene, int maxCounts, int time)
            throws FrequencyRuntimeException;

    /**
     * 指定时间内，某个key，在某个场景下，被调用的次数检查，超过最大次数时抛出异常
     *
     * @param key       某个主体标识
     * @param scene     某个场景，比如注册，登录
     * @param maxCounts 指定时间内允许的最大调用次数
     * @param increment 每调用一次，计数的增量，默认是1
     * @param time      指定时间，秒为单位
     * @throws FrequencyRuntimeException
     */
    void timeCheck(String key, String scene, int maxCounts, int increment,
                   int time) throws FrequencyRuntimeException;

    /**
     * 按照指定策略执行频率检查
     *
     * @param key             键值
     * @param scene           使用场景
     * @param maxDayCounts    一天最大次数
     * @param maxHourCounts   一小时最大次数
     * @param maxMinuteCounts 一分钟最大次数
     */
    void frequencyChecker(String key, String scene, int maxDayCounts, int maxHourCounts, int maxMinuteCounts);

}
