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
package wang.bigbird.domain.framework.core.base.tool;

import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.core.base.util.DateUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * 农历
 *
 * @author Bigbird
 */
@Slf4j
public class LunarCalendar {

    private static int year;
    private static int month;
    private static int day;

    /**
     * 闰的是哪个月
     */
    private static int leapMonth = 0;

    private final static String chineseNumber[] = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"};

    private final static long[] lunarInfo = new long[]{
            0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0,
            0x055d2, 0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2,
            0x095b0, 0x14977, 0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60,
            0x09570, 0x052f2, 0x04970, 0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60,
            0x186e3, 0x092e0, 0x1c8d7, 0x0c950, 0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4,
            0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, 0x06ca0, 0x0b550, 0x15355, 0x04da0,
            0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0, 0x0aea6, 0x0ab50, 0x04b60,
            0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0, 0x096d0, 0x04dd5,
            0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6, 0x095b0,
            0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
            0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5,
            0x092e0, 0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0,
            0x092d0, 0x0cab5, 0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0,
            0x15176, 0x052b0, 0x0a930, 0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6,
            0x0a4e0, 0x0d260, 0x0ea65, 0x0d530, 0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0,
            0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, 0x0b5a0, 0x056d0, 0x055b2, 0x049b0,
            0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0};

    /**
     * 农历部分节假日
     */
    private final static String[] lunarHoliday = new String[]{
            "0100 除夕", "0101 春节", "0115 元宵节", "0202 龙抬头", "0505 端午节", "0707 七夕节", "0715 中元节", "0815 中秋节",
            "0909 重阳节", "1208 腊八节", "1224 小年"};

    /**
     * 公历部分节假日
     */
    private final static String[] solarHoliday = new String[]{
            "0101 元旦节", "0214 情人节", "0308 妇女节", "0312 植树节", "0315 消费者权益日", "0401 愚人节", "0501 劳动节", "0504 青年节",
            "0512 护士节", "0601 儿童节", "0701 建党节", "0801 建军节", "0909 毛泽东逝世纪念日", "0910 教师节", "0928 孔子诞辰纪念日",
            "1001 国庆节", "1006 老人节", "1024 联合国日", "1101 万圣节", "1111 双11", "1112 孙中山诞辰纪念日", "1212 双12", "1220 澳门回归纪念日", "1224 平安夜", "1225 圣诞节", "1226 毛泽东诞辰纪念日"};

    /**
     * 传回农历指定年份的总天数
     *
     * @param year 指定年份
     * @return 天数
     */
    private final static int yearDays(int year) {
        int i, sum = 348;
        for (i = 0x8000; i > 0x8; i >>= 1) {
            if ((lunarInfo[year - 1900] & i) != 0) {
                sum += 1;
            }
        }
        return (sum + leapDays(year));
    }

    /**
     * 传回农历指定年份闰月的天数
     *
     * @param year 指定年份
     * @return 天数
     */
    private final static int leapDays(int year) {
        if (leapMonth(year) != 0) {
            if ((lunarInfo[year - 1900] & 0x10000) != 0) {
                return 30;
            } else {
                return 29;
            }
        } else {
            return 0;
        }
    }

    /**
     * 传回农历指定年份闰哪个月1-12，没闰传回 0
     *
     * @param year 指定年份
     * @return 月份
     */
    private final static int leapMonth(int year) {
        int result = (int) (lunarInfo[year - 1900] & 0xf);
        return result;
    }

    /**
     * 传回农历指定年份指定月份的总天数
     *
     * @param year  指定年份
     * @param month 指定月份
     * @return 天数
     */
    private final static int monthDays(int year, int month) {
        if ((lunarInfo[year - 1900] & (0x10000 >> month)) == 0) {
            return 29;
        } else {
            return 30;
        }
    }

    /**
     * 判断是否是清明节
     *
     * @param year  年
     * @param month 月
     * @param day   日
     * @return 是否是清明节
     */
    private static boolean isTombSweeping(int year, int month, int day) {
        if (month != 4) {
            return false;
        }
        if (day != 4 && day != 5 && day != 6) {
            return false;
        }
        int tempYear = (year % 10) + (((year / 10) % 10) * 10);
        int tombSweepingDay = (int) ((tempYear * 0.2422 + 4.81) - (tempYear / 4));
        if (tombSweepingDay == day) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是母亲节或者父亲节
     *
     * @param year  年
     * @param month 月
     * @param day   日
     * @return 母亲节或者父亲节描述
     */
    private static String getMotherOrFatherDay(int year, int month, int day) {
        if (month != 5 && month != 6) {
            return null;
        }
        if ((month == 5 && (day < 8 || day > 14)) || (month == 6 && (day < 15 || day > 21))) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        int weekDate = calendar.get(Calendar.DAY_OF_WEEK);
        weekDate = (weekDate == 1) ? 7 : weekDate - 1;
        switch (month) {
            case 5:
                if (day == 15 - weekDate) {
                    return "母亲节";
                }
                break;
            case 6:
                if (day == 22 - weekDate) {
                    return "父亲节";
                }
                break;
        }
        return null;
    }

    /**
     * 获取y年m月d日对应的农历日期描述
     *
     * @param year_log  指定年
     * @param month_log 指定月
     * @param day_log   指定日
     * @param isDay     false - 日期为节假日时，阴历日期就返回节假日；true - 不管日期是否为节假日依然返回这天对应的阴历日期
     * @return 阴历日期描述
     */
    private final static String getLunarDate(int year_log, int month_log, int day_log,
                                             boolean isDay) {
        Date baseDate = DateUtils.parse("1900年1月31日", DateUtils.CHINESE_DATE_FORMAT_PATTERN);
        Date nowaday = DateUtils.parse(StringUtils.joinStr(year_log, "年", month_log, "月", day_log, "日"), DateUtils.CHINESE_DATE_FORMAT_PATTERN);
        // 求出和1900年1月31日相差的天数
        int offset = (int) ((nowaday.getTime() - baseDate.getTime()) / 86400000L);
        // 用offset减去每农历年的天数
        // 计算当天是农历第几天
        // i最终结果是农历的年份
        // offset是当年的第几天
        int iYear, daysOfYear = 0;
        for (iYear = 1900; iYear < 10000 && offset > 0; iYear++) {
            daysOfYear = yearDays(iYear);
            offset -= daysOfYear;
        }
        if (offset < 0) {
            offset += daysOfYear;
            iYear--;
        }
        // 农历年份
        year = iYear;
        // 闰哪个月，1-12
        leapMonth = leapMonth(iYear);
        boolean leap = false;
        // 用当年的天数offset，逐个减去每月（农历）的天数，求出当天是本月的第几天
        int iMonth, daysOfMonth = 0;
        for (iMonth = 1; iMonth < 13 && offset > 0; iMonth++) {
            // 闰月
            if (leapMonth > 0 && iMonth == (leapMonth + 1) && !leap) {
                --iMonth;
                leap = true;
                daysOfMonth = leapDays(year);
            } else {
                daysOfMonth = monthDays(year, iMonth);
            }
            offset -= daysOfMonth;
            // 解除闰月
            if (leap && iMonth == (leapMonth + 1)) {
                leap = false;
            }
        }
        // offset为0时，并且刚才计算的月份是闰月，要校正
        if (offset == 0 && leapMonth > 0 && iMonth == leapMonth + 1) {
            if (!leap) {
                --iMonth;
            }
        }
        // offset小于0时也要校正
        if (offset < 0) {
            offset += daysOfMonth;
            --iMonth;
        }
        month = iMonth;
        // 设置对应的阴历月份
        day = offset + 1;
        if (!isDay) {
            // 如果日期为节假日则阴历日期则返回节假日
            for (int i = 0; i < solarHoliday.length; i++) {
                // 返回公历节假日名称
                // 节假日的日期
                String sd = solarHoliday[i].split(" ")[0];
                // 节假日的名称
                String sdv = solarHoliday[i].split(" ")[1];
                String smonth_v = month_log + "";
                String sday_v = day_log + "";
                String smd = "";
                if (month_log < 10) {
                    smonth_v = "0" + month_log;
                }
                if (day_log < 10) {
                    sday_v = "0" + day_log;
                }
                smd = smonth_v + sday_v;
                if (sd.trim().equals(smd.trim())) {
                    return sdv;
                }
            }
            for (int i = 0; i < lunarHoliday.length; i++) {
                // 返回农历节假日名称
                // 节假日的日期
                String ld = lunarHoliday[i].split(" ")[0];
                // 节假日的名称
                String ldv = lunarHoliday[i].split(" ")[1];
                String lmonth_v = month + "";
                String lday_v = day + "";
                String lmd = "";
                if (month < 10) {
                    lmonth_v = "0" + month;
                }
                if (day < 10) {
                    lday_v = "0" + day;
                }
                lmd = lmonth_v + lday_v;
                if ("12".equals(lmonth_v)) {
                    // 除夕夜需要特殊处理
                    if ((daysOfMonth == 29 && day == 29) || (daysOfMonth == 30 && day == 30)) {
                        return ldv;
                    }
                }
                if (ld.trim().equals(lmd.trim())) {
                    return ldv;
                }
            }
            if (isTombSweeping(year_log, month_log, day_log)) {
                return "清明";
            }
            String motherOrFatherDay = getMotherOrFatherDay(year_log, month_log, day_log);
            if (motherOrFatherDay != null) {
                return motherOrFatherDay;
            }
        }
        if (day == 1) {
            return chineseNumber[month - 1] + "月";
        } else {
            return getChinaDayString(day);
        }
    }

    /**
     * 传入月日的offset，传回干支，0=甲子
     *
     * @param num 偏移量
     * @return 干支
     */
    private final static String gz(int num) {
        final String[] Gan = new String[]{"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
        final String[] Zhi = new String[]{"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
        return (Gan[num % 10] + Zhi[num % 12]);
    }

    /**
     * 传入指定年份，传回干支，0=甲子
     *
     * @param year 指定年份
     * @return
     */
    public final static String cyclical(int year) {
        int num = year - 1900 + 36;
        return (gz(num));
    }

    /**
     * 传回农历指定年份的生肖
     *
     * @param year 指定年份
     * @return 生肖
     */
    public final static String animalsYear(int year) {
        final String[] Animals = new String[]{"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};
        return Animals[(year - 4) % 12];
    }

    /**
     * 获取农历日
     *
     * @param day 指定天数
     * @return 农历日
     */
    public final static String getChinaDayString(int day) {
        String chineseTen[] = {"初", "十", "廿", "卅"};
        int n = day % 10 == 0 ? 9 : day % 10 - 1;
        if (day > 30) {
            return "";
        }
        if (day == 10) {
            return "初十";
        } else {
            String des = chineseTen[day / 10] + chineseNumber[n];
            if (des.equalsIgnoreCase("廿十")) {
                return "二十";
            }
            return des;
        }
    }

    /**
     * 获取指定日期的阴历日期描述
     *
     * @param date  指定日期
     * @param isDay false - 日期为节假日时，阴历日期就返回节假日；true - 不管日期是否为节假日依然返回这天对应的阴历日期
     * @return 阴历日期描述
     */
    public static String dateToLunarDate(Date date, boolean isDay) {
        String dateString = DateUtils.format(date, DateUtils.DATE_PATTERN);
        String[] dates = dateString.split("-");
        return getLunarDate(Integer.parseInt(dates[0]), Integer.parseInt(dates[1]), Integer.parseInt(dates[2]), isDay);
    }

}
