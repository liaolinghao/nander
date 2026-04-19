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
package wang.bigbird.domain.framework.core.base.util;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 日期操作工具类
 *
 * @author Bigbird
 */
public class DateUtils {

    /**
     * 禁止实例化
     */
    private DateUtils() {
        throw new IllegalStateException();
    }

    /**
     * 中文日期格式
     */
    public static final String CHINESE_DATE_FORMAT_PATTERN = "yyyy年MM月dd日";
    /**
     * 年月日时分秒格式
     */
    public static final String STANDARD_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /**
     * 年月日格式，hh代表12小时制，HH代表24小时制，mm代表分钟，MM代表月份。
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    /**
     * 时分秒格式，hh代表12小时制，HH代表24小时制，mm代表分钟，MM代表月份。
     */
    public static final String TIME_PATTERN = "HH:mm:ss";
    /**
     * GMT时间格式化阈值
     */
    private static final int GMT_TIME_THRESHOLD = 10;

    /**
     * 地区ID
     */
    private static ZoneId zoneId = ZoneId.systemDefault();

    private static final String GMT_TIMEZONE_ID = "GMT";

    /**
     * Parse the given {@code timeZoneString} value into a {@link TimeZone}.
     *
     * @param timeZoneString the time zone {@code String}, following {@link TimeZone#getTimeZone(String)}
     *                       but throwing {@link IllegalArgumentException} in case of an invalid time zone specification
     * @return a corresponding {@link TimeZone} instance
     * @throws IllegalArgumentException in case of an invalid time zone specification
     */
    public static TimeZone parseTimeZoneString(String timeZoneString) {
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneString);
        if (GMT_TIMEZONE_ID.equals(timeZone.getID()) && !timeZoneString.startsWith(GMT_TIMEZONE_ID)) {
            // We don't want that GMT fallback...
            throw new IllegalArgumentException("Invalid time zone specification '" + timeZoneString + "'");
        }
        return timeZone;
    }

    /**
     * 取得当前时间表示的字符串,该字符串用于需要时间戳的应用中
     *
     * @param needTimeZone 是否需要时区标志，需要时区标志时，字符串显示形式如：&TimeStamp=2011-12-23
     *                     13:38:52&GMT=08:00
     * @return 当前时间戳
     */
    public static String getTimeStamp(boolean needTimeZone) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(STANDARD_PATTERN);
        String str = LocalDateTime.now().format(formatter);
        if (needTimeZone) {
            str = "&TimeStamp=" + str;
            float gmt = TimeZone.getDefault().getRawOffset() / 3600000;
            int g = (int) gmt;
            int m = (int) ((gmt - (int) gmt) * 100);
            if (g < GMT_TIME_THRESHOLD) {
                str = str + "&GMT=0" + g + ":";
            } else {
                str = str + "&GMT=" + g + ":";
            }
            if (m < GMT_TIME_THRESHOLD) {
                str = str + "0" + m;
            } else {
                str = str + m;
            }
        }
        return str;
    }

    /**
     * 时间字符串转Date对象
     *
     * @param text    时间字符串
     * @param pattern 时间格式串
     * @return 时间对象
     */
    @SneakyThrows
    public static Date parse(String text, String pattern) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.parse(text);
    }

    /**
     * 时间格式化
     *
     * @param date    时间对象
     * @param pattern 时间格式串
     * @return 时间字符串
     */
    public static String format(Date date, String pattern) {
        if (null == date) {
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.format(date);
    }

    /**
     * 给时间增加指定的月份数
     *
     * @param dt     时间对象
     * @param months 调整月份数
     * @return 调整后时间
     */
    public static Date addMonths(Date dt, int months) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.add(Calendar.MONTH, months);
        return cal.getTime();
    }

    /**
     * 给时间增加指定的天数
     *
     * @param dt   时间对象
     * @param days 调整天数
     * @return 调整后时间
     */
    public static Date addDays(Date dt, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    /**
     * 给时间增加指定的小时数
     *
     * @param dt    时间对象
     * @param hours 调整小时数
     * @return 调整后时间
     */
    public static Date addHours(Date dt, int hours) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.add(Calendar.HOUR, hours);
        return cal.getTime();
    }

    /**
     * 给时间增加指定的分钟数
     *
     * @param dt      时间对象
     * @param minutes 调整分钟数
     * @return 调整后时间
     */
    public static Date addMinutes(Date dt, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.add(Calendar.MINUTE, minutes);
        return cal.getTime();
    }

    /**
     * 给时间增加指定的秒数
     *
     * @param dt      时间对象
     * @param seconds 调整秒数
     * @return 调整后时间
     */
    public static Date addSeconds(Date dt, int seconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.add(Calendar.SECOND, seconds);
        return cal.getTime();
    }

    /**
     * 给时间增加指定的毫秒数
     *
     * @param dt           时间对象
     * @param milliSeconds 调整毫秒数
     * @return 调整后时间
     */
    public static Date addMilliSeconds(Date dt, int milliSeconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.add(Calendar.MILLISECOND, milliSeconds);
        return cal.getTime();
    }

    /**
     * 检查日期格式是否是合法日期
     *
     * @param dateStr 日期
     * @return 是否合法
     */
    public static boolean checkDateValid(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return false;
        }
        SimpleDateFormat date = new SimpleDateFormat(DATE_PATTERN);
        if (dateStr.length() == STANDARD_PATTERN.length()) {
            date = new SimpleDateFormat(STANDARD_PATTERN);
        }
        try {
            date.setLenient(false);
            date.parse(dateStr);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 计算两个日期之间相差的秒数，严格计算模式，不满跨度值不纳入计数
     *
     * @param startDate 较小的时间
     * @param endDate   较大的时间
     * @return 相差秒数
     * @throws ParseException
     */
    public static long secondsBetween(String startDate, String endDate)
            throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(STANDARD_PATTERN);
        return secondsBetween(sdf.parse(startDate), sdf.parse(endDate));
    }

    /**
     * 计算两个日期之间相差的秒数，严格计算模式，不满跨度值不纳入计数
     *
     * @param startDate 较小的时间
     * @param endDate   较大的时间
     * @return 相差秒数
     */
    public static long secondsBetween(Date startDate, Date endDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(endDate);
        long time2 = cal.getTimeInMillis();
        return (time2 - time1) / 1000;
    }

    /**
     * 计算两个日期之间相差的分钟数，严格计算模式，不满跨度值不纳入计数
     *
     * @param startDate 较小的时间
     * @param endDate   较大的时间
     * @return 相差分钟数
     * @throws ParseException
     */
    public static long minutesBetween(String startDate, String endDate)
            throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        return minutesBetween(sdf.parse(startDate), sdf.parse(endDate));
    }

    /**
     * 计算两个日期之间相差的分钟数，严格计算模式，不满跨度值不纳入计数
     *
     * @param startDate 较小的时间
     * @param endDate   较大的时间
     * @return 相差分钟数
     */
    public static long minutesBetween(Date startDate, Date endDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(endDate);
        long time2 = cal.getTimeInMillis();
        return (time2 - time1) / (1000 * 60);
    }

    /**
     * 计算两个日期之间相差的天数，严格计算模式，不满跨度值不纳入计数
     *
     * @param startDate 较小的时间
     * @param endDate   较大的时间
     * @return 相差天数
     * @throws ParseException
     */
    public static long daysBetween(String startDate, String endDate)
            throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        return daysBetween(sdf.parse(startDate), sdf.parse(endDate));
    }

    /**
     * 计算两个日期之间相差的天数，严格计算模式，不满跨度值不纳入计数
     *
     * @param startDate 较小的时间
     * @param endDate   较大的时间
     * @return 相差天数
     */
    public static long daysBetween(Date startDate, Date endDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(endDate);
        long time2 = cal.getTimeInMillis();
        return (time2 - time1) / (1000 * 3600 * 24);
    }

    /**
     * 计算两个日期之间相差的月数，严格计算模式，不满跨度值不纳入计数
     *
     * @param startDate 较小的时间
     * @param endDate   较大的时间
     * @return 相差月数
     * @throws ParseException
     */
    public static long monthBetween(String startDate, String endDate)
            throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        return monthBetween(sdf.parse(startDate), sdf.parse(endDate));
    }

    /**
     * 计算两个日期之间相差的月数，严格计算模式，不满跨度值不纳入计数
     *
     * @param startDate 较小的时间
     * @param endDate   较大的时间
     * @return 相差月数
     */
    public static long monthBetween(Date startDate, Date endDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        long months = 0;
        while (true) {
            ++months;
            // 逐月递增
            cal.add(Calendar.MONTH, 1);
            if (cal.getTime().after(endDate)) {
                months--;
                break;
            }
        }
        return months;
    }

    /**
     * 计算两个日期之间相差的年数，严格计算模式，不满跨度值不纳入计数
     *
     * @param startDate 较小的时间
     * @param endDate   较大的时间
     * @return 相差年数
     * @throws ParseException
     */
    public static long yearsBetween(String startDate, String endDate)
            throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        return yearsBetween(sdf.parse(startDate), sdf.parse(endDate));
    }

    /**
     * 计算两个日期之间相差的年数，严格计算模式，不满跨度值不纳入计数
     *
     * @param startDate 较小的时间
     * @param endDate   较大的时间
     * @return 相差年数
     */
    public static long yearsBetween(Date startDate, Date endDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        long years = 0;
        while (true) {
            ++years;
            // 逐年递增
            cal.add(Calendar.YEAR, 1);
            if (cal.getTime().after(endDate)) {
                years--;
                break;
            }
        }
        return years;
    }

    /**
     * 调整日期到指定时刻
     *
     * @param date   日期
     * @param hour   小时
     * @param minute 分钟
     * @param second 秒钟
     * @return 指定时间
     */
    public static Date adjustDate(Date date, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        return calendar.getTime();
    }

    /**
     * 调整日期到当前月指定日指定时刻
     *
     * @param date   日期
     * @param day    日
     * @param hour   小时
     * @param minute 分钟
     * @param second 秒钟
     * @return 指定时间
     */
    public static Date adjustDate(Date date, int day, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        return calendar.getTime();
    }

    /**
     * 获取上个月起始日期
     *
     * @return 上个月起始日期
     */
    public static Date getLastMonthStartDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取上个月结束日期
     *
     * @return 上个月结束日期
     */
    public static Date getLastMonthEndDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }

    /**
     * 获取指定年月的起始时间
     *
     * @param year  指定年
     * @param month 指定月
     * @return 指定年月的起始时间
     */
    public static Date getFirstDayOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取指定年月的结束时间
     *
     * @param year  指定年
     * @param month 指定月
     * @return 指定年月的结束时间
     */
    public static Date getEndDayOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }

    /**
     * 将Date转换为LocalDatetime
     *
     * @param date 日期
     * @return 对应LocalDateTime类型的日期
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        return instant.atZone(zoneId).toLocalDateTime();
    }

    /**
     * 获取当前年份
     *
     * @return
     */
    public static int getThisYear() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(STANDARD_PATTERN);
        String str = LocalDateTime.now().format(formatter);
        return Integer.parseInt(str.substring(0, str.indexOf("-")));
    }

    /**
     * 获取指定日期在一年中的索引位置
     *
     * @param date 日期
     * @return 指定日期在一年中的索引位
     */
    public static int getCurrentDayIndexInYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * 转换日期为当日的开始时刻
     *
     * @param dateStr 日期
     * @return 当日的开始时刻
     */
    public static String convert2DayStartTime(String dateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        if (dateStr.length() == STANDARD_PATTERN.length()) {
            dateFormat = new SimpleDateFormat(STANDARD_PATTERN);
        }
        dateFormat.setLenient(false);
        try {
            Date date = dateFormat.parse(dateStr);
            return format(adjustDate(date, 0, 0, 0), STANDARD_PATTERN);
        } catch (ParseException pe) {
            throw new IllegalArgumentException(pe);
        }
    }

    /**
     * 转换日期为当日的结束时刻
     *
     * @param dateStr 日期
     * @return 当日的结束时刻
     */
    public static String convert2DayEndTime(String dateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        if (dateStr.length() == STANDARD_PATTERN.length()) {
            dateFormat = new SimpleDateFormat(STANDARD_PATTERN);
        }
        dateFormat.setLenient(false);
        try {
            Date date = dateFormat.parse(dateStr);
            return format(adjustDate(date, 23, 59, 59), STANDARD_PATTERN);
        } catch (ParseException pe) {
            throw new IllegalArgumentException(pe);
        }
    }

    /**
     * 获取当前时刻距离当天结束的剩余时间，以毫秒为单位
     *
     * @return 当前时刻距离当天结束的剩余毫秒时间
     */
    public static long getMillisUntilMidnight() {
        // 获取当前时间（带时区）
        ZonedDateTime now = ZonedDateTime.now();
        // 构建当天结束时间（次日凌晨0点）
        ZonedDateTime midnight = now.toLocalDate()
                .plusDays(1)
                .atStartOfDay(now.getZone());
        // 计算毫秒差
        return ChronoUnit.MILLIS.between(now, midnight);
    }

}
