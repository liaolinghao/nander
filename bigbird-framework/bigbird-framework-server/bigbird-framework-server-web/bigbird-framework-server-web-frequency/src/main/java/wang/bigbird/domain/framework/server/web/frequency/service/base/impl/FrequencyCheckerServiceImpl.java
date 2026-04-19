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
package wang.bigbird.domain.framework.server.web.frequency.service.base.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.util.DateUtils;
import wang.bigbird.domain.framework.data.redis.service.base.IRedisService;
import wang.bigbird.domain.framework.server.web.frequency.exception.*;
import wang.bigbird.domain.framework.server.web.frequency.service.base.IFrequencyCheckerService;

import java.util.concurrent.TimeUnit;

/**
 * 频率检查器
 *
 * @author Bigbird
 */
@Slf4j
@Service
public class FrequencyCheckerServiceImpl implements IFrequencyCheckerService {

    private static final String CHECK_DAY_PRE = "check:day:";
    private static final String CHECK_HOUR_PRE = "check:hour:";
    private static final String CHECK_MINUTE_PRE = "check:minute:";
    private static final String CHECK_SAME_DAY_PRE = "check:same:day:";
    private static final String CHECK_TIME_PRE = "check:time:";

    @Autowired
    private IRedisService redisService;

    /**
     * 获取计数值，并对redisKey的计数增加increment，如果redisKey不存在，value设置为increment
     *
     * @param redisKey
     * @param increment 增长值
     * @return 计数值
     */
    private long getCount(String redisKey, int increment) {
        long totalCounts = redisService.incrby(redisKey, increment);
        return totalCounts - increment;
    }

    @Override
    public void dayCheck(String key, String scene, int maxCounts)
            throws ExceedMaxTimesOneDayException {
        dayCheck(key, scene, maxCounts, 1);
    }

    @Override
    public void dayCheck(String key, String scene, int maxCounts, int increment)
            throws ExceedMaxTimesOneDayException {
        String redisKey = CHECK_DAY_PRE + key + ":" + scene;
        long totalCount = getCount(redisKey, increment);
        if (totalCount >= maxCounts) {
            log.warn("{}-{}:exceed max times in one day.", key, scene);
            throw new ExceedMaxTimesOneDayException();
        }
        expireRedisKey(redisKey, 1, TimeUnit.DAYS);
    }

    @Override
    public void hourCheck(String key, String scene, int maxCounts)
            throws ExceedMaxTimesOneHourException {
        hourCheck(key, scene, maxCounts, 1);
    }

    @Override
    public void hourCheck(String key, String scene, int maxCounts, int increment)
            throws ExceedMaxTimesOneHourException {
        String redisKey = CHECK_HOUR_PRE + key + ":" + scene;
        long totalCount = getCount(redisKey, increment);
        if (totalCount >= maxCounts) {
            log.warn("{}-{}:exceed max times in one hour.", key, scene);
            throw new ExceedMaxTimesOneHourException();
        }
        expireRedisKey(redisKey, 1, TimeUnit.HOURS);
    }

    @Override
    public void minuteCheck(String key, String scene, int maxCounts)
            throws ExceedMaxTimesOneMinuteException {
        minuteCheck(key, scene, maxCounts, 1);
    }

    @Override
    public void minuteCheck(String key, String scene, int maxCounts,
                            int increment) throws ExceedMaxTimesOneMinuteException {
        String redisKey = CHECK_MINUTE_PRE + key + ":" + scene;
        long totalCount = getCount(redisKey, increment);
        if (totalCount >= maxCounts) {
            log.warn("{}-{}:exceed max times in one minute.", key, scene);
            throw new ExceedMaxTimesOneMinuteException();
        }
        expireRedisKey(redisKey, 1, TimeUnit.MINUTES);
    }

    @Override
    public void sameDayCheck(String key, String scene, int maxCounts) throws ExceedMaxTimesSameDayException {
        sameDayCheck(key, scene, maxCounts, 1);
    }

    @Override
    public void sameDayCheck(String key, String scene, int maxCounts, int increment) throws ExceedMaxTimesSameDayException {
        String redisKey = CHECK_SAME_DAY_PRE + key + ":" + scene;
        long totalCount = getCount(redisKey, increment);
        if (totalCount >= maxCounts) {
            log.warn("{}-{}:exceed max times in same day.", key, scene);
            throw new ExceedMaxTimesOneMinuteException();
        }
        expireRedisKey(redisKey, DateUtils.getMillisUntilMidnight(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void timeCheck(String key, String scene, int maxCounts, int time)
            throws FrequencyRuntimeException {
        timeCheck(key, scene, maxCounts, 1, time);
    }

    @Override
    public void timeCheck(String key, String scene, int maxCounts,
                          int increment, int time) throws FrequencyRuntimeException {
        String redisKey = CHECK_TIME_PRE + key + ":" + scene;
        long totalCount = getCount(redisKey, increment);
        if (totalCount >= maxCounts) {
            log.warn("{}-{}:exceed max times in custom time.", key, scene);
            throw new FrequencyRuntimeException();
        }
        expireRedisKey(redisKey, time, TimeUnit.SECONDS);
    }

    @Override
    public void frequencyChecker(String key, String scene, int maxDayCounts, int maxHourCounts, int maxMinuteCounts) {
        minuteCheck(key, scene, maxMinuteCounts);
        hourCheck(key, scene, maxHourCounts);
        dayCheck(key, scene, maxDayCounts);
    }

    /**
     * 为redisKey设置有效期，如果redisKey没有有效期则设置有效期，如果已经设置有效期则保持有效期不变
     *
     * @param redisKey 键
     * @param expire   过期时间值
     * @param timeUnit 过期时间单位
     */
    private void expireRedisKey(String redisKey, long expire, TimeUnit timeUnit) {
        if (redisService.ttl(redisKey) == -1) {
            redisService.expire(redisKey, expire, timeUnit);
        }
    }

}
