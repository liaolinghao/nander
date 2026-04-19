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
package wang.bigbird.domain.framework.cache.support;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.support.SimpleCacheManager;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;

import java.time.Duration;

/**
 * 自定义缓存管理器，可根据缓存名称对缓存时间做特殊设置
 *
 * @author Bigbird
 */
@Slf4j
public class CustomizedCacheManager extends SimpleCacheManager {

    /**
     * 解析名称中是否包含ttl的特别设置，没有的话，就保持默认值
     *
     * @param cacheName
     * @param defaultValue 毫秒为单位
     * @return
     */
    protected long parseTTL(String cacheName, long defaultValue) {
        Assert.hasText(cacheName, "The cacheName must not be blank.");
        int index = StringUtils.indexOf(cacheName, CommonConstants.Cache.CACHE_NAME_SEPARATOR);
        if (index > 0) {
            cacheName = cacheName.substring(index + CommonConstants.Cache.CACHE_NAME_SEPARATOR.length());
            String[] values = cacheName.split(CommonConstants.Cache.CACHE_NAME_SEPARATOR);
            try {
                return parseDateExpressStr(values[0]);
            } catch (Exception e) {
                log.error("Resolves ttl errors from the cacheName: {}.", cacheName, e);
            }
        }
        return defaultValue;
    }

    /**
     * 解析名称中是否包含maxIdleTime的特别设置，没有的话，就保持默认值
     *
     * @param cacheName
     * @param defaultValue 毫秒为单位
     * @return
     */
    protected long parseMaxIdleTime(String cacheName, long defaultValue) {
        Assert.hasText(cacheName, "The cacheName must not be blank.");
        int index = StringUtils.indexOf(cacheName, CommonConstants.Cache.CACHE_NAME_SEPARATOR);
        if (index > 0) {
            cacheName = cacheName.substring(index + CommonConstants.Cache.CACHE_NAME_SEPARATOR.length());
            String[] values = cacheName.split(CommonConstants.Cache.CACHE_NAME_SEPARATOR);
            try {
                if (values.length > 1) {
                    return parseDateExpressStr(values[1]);
                }
            } catch (Exception e) {
                log.error("Resolves maxIdleTime errors from the cacheName: {}.", cacheName, e);
            }
        }
        return defaultValue;
    }

    private long parseDateExpressStr(String value) {
        char lastChar = Character.toLowerCase(value.charAt(value.length() - 1));
        switch (lastChar) {
            case 'd':
                return Duration.ofDays(parseDuration(value)).toMillis();
            case 'h':
                return Duration.ofHours(parseDuration(value)).toMillis();
            case 'm':
                return Duration.ofMinutes(parseDuration(value)).toMillis();
            case 's':
                return Duration.ofSeconds(parseDuration(value)).toMillis();
            default:
                return Long.parseLong(value);
        }
    }

    private long parseDuration(String value) {
        return Long.parseLong(value.substring(0, value.length() - 1));
    }

}
