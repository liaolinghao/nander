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
package wang.bigbird.domain.framework.server.web.quartz.base.tool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Cron表达式生成工具类
 *
 * @author Bigbird
 */
public class CronExpressionGenerator {

    /**
     * 时间起始值
     */
    private static final int zero = 0;
    /**
     * 最大小时数
     */
    private static final int max_hour = 23;
    /**
     * 最大分钟数
     */
    private static final int max_minute = 59;
    /**
     * 最大秒数
     */
    private static final int max_second = 59;
    /**
     * 精确执行一次的cron表达式格式
     */
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ss mm HH dd MM ? yyyy");

    /**
     * 根据LocalDateTime构造表示精确执行一次的cron表达式
     * 生成的表达式格式为：ss mm HH dd MM ? yyyy
     *
     * @param dateTime 时间
     * @return 对应的cron表达式
     */
    public static String generateCron(LocalDateTime dateTime) {
        return dateTime.format(formatter);
    }

    /**
     * 根据时分秒构造cron表达式
     * 生成的表达式格式为: 秒 分 时 * * ?
     * 表示每天的指定时分秒执行
     *
     * @param hour   小时，0-23
     * @param minute 分钟，0-59
     * @param second 秒，0-59
     * @return 对应的cron表达式
     * @throws IllegalArgumentException 当参数不合法时抛出
     */
    public static String generateCron(int hour, int minute, int second) {
        // 验证参数合法性
        validateTimeParameters(hour, minute, second);
        // 构造cron表达式: 秒 分 时 * * ?
        return String.format("%d %d %d * * ?", second, minute, hour);
    }

    /**
     * 验证时间参数的合法性
     *
     * @param hour   小时
     * @param minute 分钟
     * @param second 秒
     * @throws IllegalArgumentException 当参数不在有效范围内时抛出
     */
    private static void validateTimeParameters(int hour, int minute, int second) {
        if (hour < zero || hour > max_hour) {
            throw new IllegalArgumentException("The hour must be between 0 and 23, current value: " + hour);
        }
        if (minute < zero || minute > max_minute) {
            throw new IllegalArgumentException("The minute must be between 0 and 59, current value: " + minute);
        }
        if (second < zero || second > max_second) {
            throw new IllegalArgumentException("The second must be between 0 and 59, current value: " + second);
        }
    }

}
