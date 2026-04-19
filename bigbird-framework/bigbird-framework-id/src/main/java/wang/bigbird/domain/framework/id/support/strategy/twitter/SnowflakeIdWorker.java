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
package wang.bigbird.domain.framework.id.support.strategy.twitter;

import wang.bigbird.domain.framework.core.base.tool.SystemClock;
import wang.bigbird.domain.framework.id.exception.IdGenerateException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

/**
 * twitter Snowflake算法，提供uid生成器
 *
 * <br>
 * SnowFlake的结构如下（每部分用-分开）：
 * <br>
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 * <br>
 * 1位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，所以id一般是正数，最高位是0
 * <br>
 * 41位时间截（毫秒级），注意，41位时间截不是存储当前时间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截）
 * <br>
 * 这里的的开始时间截，一般是我们的id生成器开始使用的时间，由我们程序来指定的（如下面程序IdWorker类的startTime属性）。
 * 41位的时间截，可以使用69年 = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69
 * <br>
 * 10位的数据机器位，可以部署在1024个节点，包括5位datacenterId和5位workerId。
 * <br>
 * 12位序列，毫秒内的计数，12位的计数顺序号支持每个节点每毫秒（同一机器，同一时间截）产生4096个ID序号。
 * <br>
 * 加起来刚好64位，为一个Long型。
 *
 * @author Bigbird
 */
public class SnowflakeIdWorker {

    /**
     * 时间回拨阈值
     */
    private final static long CALLBACK_LIMIT = 5L;

    private final static String ERROR_ATTR_LIMIT = "The scope of the property %s is 0-%d.";

    private final static String MSG_UID_PARSE = "{\"UID\":\"%s\",\"timestamp\":\"%s\",\"workerId\":\"%d\",\"dataCenterId\":\"%d\",\"sequence\":\"%d\"}";

    private final static String DATE_PATTERN_DEFAULT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 开始时间截（1984-12-22 23:59:59），用于用当前时间戳减去这个时间戳，算出偏移量
     */
    private final long epoch = 472579199000L;

    /**
     * 机器id所占的位数（表示只允许workId的范围为：0-1023）
     */
    private final long workerIdBits = 5L;

    /**
     * 数据标识id所占的位数
     */
    private final long datacenterIdBits = 5L;

    /**
     * 支持的最大机器id，结果是31（这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数）
     */
    public final long maxWorkerId = -1L ^ (-1L << workerIdBits);

    /**
     * 支持的最大数据标识id，结果是31
     */
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    /**
     * 序列在id中占的位数（表示允许sequenceId的范围为：0-4095）
     */
    private final long sequenceBits = 12L;

    /**
     * 机器ID向左移12位
     */
    private final long workerIdShift = sequenceBits;

    /**
     * 数据标识id向左移17位（12+5）
     */
    private final long datacenterIdShift = sequenceBits + workerIdBits;

    /**
     * 时间截向左移22位（5+5+12）
     */
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    /**
     * 生成序列的掩码，（防止溢出：位与运算保证计算的结果范围始终是 0-4095，0b111111111111=0xfff=4095)
     */
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    /**
     * 工作机器ID（0~31）
     */
    private long workerId;

    /**
     * 数据中心ID（0~31）
     */
    private long datacenterId;

    /**
     * 毫秒内序列（0~4095）
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;

    public SnowflakeIdWorker(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format(ERROR_ATTR_LIMIT, "workerId", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format(ERROR_ATTR_LIMIT, "datacenterId", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 获得下一个ID（该方法是线程安全的）
     *
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = SystemClock.now();
        // 闰秒：如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= CALLBACK_LIMIT) {
                try {
                    // 时间偏差小于5ms，则等待两倍时间
                    wait(offset << 1);
                    timestamp = SystemClock.now();
                    if (timestamp < lastTimestamp) {
                        // 还是小于，抛异常并上报
                        throw new IdGenerateException(String.format(IdGenerateException.ERROR_CLOCK_BACK, lastTimestamp - timestamp));
                    }
                } catch (InterruptedException e) {
                    throw new IdGenerateException(e);
                }
            } else {
                throw new IdGenerateException(String.format(IdGenerateException.ERROR_CLOCK_BACK, lastTimestamp - timestamp));
            }
        }
        // 解决跨毫秒生成ID序列号始终为偶数的缺陷：如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            // 通过位与运算保证计算的结果范围始终是 0-4095
            sequence = (sequence + 1) & sequenceMask;
            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒，获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 时间戳改变，毫秒内序列重置
            sequence = ThreadLocalRandom.current().nextLong(0, 4096);
        }
        // 上次生成ID的时间截
        lastTimestamp = timestamp;
        /*
         * 1.左移运算是为了将数值移动到对应的段
         * 2.然后对每个左移后的值做位或运算，是为了把各个短的数据合并起来，合并成一个二进制数
         * 3.最后转换成10进制，就是最终生成的id（64位的ID)
         */
        return ((timestamp - epoch) << timestampLeftShift) | (datacenterId << datacenterIdShift) | (workerId << workerIdShift) | sequence;
    }

    /**
     * 反解析UID
     *
     * @param uid
     * @return uid信息结构说明
     */
    public String parseUid(Long uid) {
        // 总位数
        long totalBits = 64L;
        // 标识
        long signBits = 1L;
        // 时间戳
        long timestampBits = 41L;
        // 解析Uid：标识 -- 时间戳 -- 数据中心 -- 机器码 --序列
        long sequence = (uid << (totalBits - sequenceBits)) >>> (totalBits - sequenceBits);
        long dataCenterId = (uid << (timestampBits + signBits)) >>> (totalBits - datacenterIdBits);
        long workerId = (uid << (timestampBits + signBits + datacenterIdBits)) >>> (totalBits - workerIdBits);
        long deltaSeconds = uid >>> (datacenterIdBits + workerIdBits + sequenceBits);
        // 时间处理(补上开始时间戳)
        Date thatTime = new Date(epoch + deltaSeconds);
        String date = new SimpleDateFormat(DATE_PATTERN_DEFAULT).format(thatTime);
        // 格式化输出
        return String.format(MSG_UID_PARSE, uid, date, workerId, dataCenterId, sequence);
    }

    /**
     * 反解析UID
     *
     * @param uid
     * @return uid信息结构说明
     */
    public String parseUid(String uid) {
        uid = Long.toBinaryString(Long.parseLong(uid));
        int len = uid.length();
        /* 解析Uid：标识 -- 时间戳 -- 数据中心 -- 机器码 --序列 */
        // sequence起始数
        int sequenceStart = len < workerIdShift ? 0 : (int) (len - workerIdShift);
        // worker起始数
        int workerStart = len < datacenterIdShift ? 0 : (int) (len - datacenterIdShift);
        // 时间起始数
        int timeStart = len < timestampLeftShift ? 0 : (int) (len - timestampLeftShift);
        String sequence = uid.substring(sequenceStart, len);
        String workerId = sequenceStart == 0 ? "0" : uid.substring(workerStart, sequenceStart);
        String dataCenterId = workerStart == 0 ? "0" : uid.substring(timeStart, workerStart);
        // 时间处理（补上开始时间戳）
        String time = timeStart == 0 ? "0" : uid.substring(0, timeStart);
        Date timeDate = new Date(Long.parseLong(time, 2) + epoch);
        String date = new SimpleDateFormat(DATE_PATTERN_DEFAULT).format(timeDate);
        // 格式化输出
        return String.format(MSG_UID_PARSE, uid, date, Integer.valueOf(workerId, 2), Integer.valueOf(dataCenterId, 2), Integer.valueOf(sequence, 2));
    }

    /**
     * 保证返回的毫秒数在参数之后（阻塞到下一个毫秒，直到获得新的时间戳）
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = SystemClock.now();
        while (timestamp <= lastTimestamp) {
            timestamp = SystemClock.now();
        }
        return timestamp;
    }
}
