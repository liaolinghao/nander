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

import com.sun.management.OperatingSystemMXBean;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.util.Util;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * 系统操作工具类
 *
 * @author Bigbird
 */
public class SystemUtils {

    /**
     * 默认比率
     */
    private static final double DEFAULT_RATIO = 1.0;

    /**
     * 获取JAVA运行环境
     *
     * @return JAVA运行环境信息
     * @throws UnknownHostException
     */
    public static Map env() throws UnknownHostException {
        Map<String, Object> result = new HashMap<>(CollectionUtils.initialMapCapacity(35));
        Runtime r = Runtime.getRuntime();
        Properties props = System.getProperties();
        InetAddress inetAddress = InetAddress.getLocalHost();
        String ip = inetAddress.getHostAddress();
        Map<String, String> map = System.getenv();
        // 获取用户名
        String userName = map.get("USERNAME");
        // 获取计算机名
        String computerName = map.get("COMPUTERNAME");
        // 获取计算机域名
        String userDomain = map.get("USERDOMAIN");
        result.put("用户名", userName);
        result.put("计算机名", computerName);
        result.put("计算机域名", userDomain);
        result.put("本地ip地址", ip);
        result.put("本地主机名", inetAddress.getHostName());
        result.put("JVM可以使用的总内存", r.totalMemory());
        result.put("JVM可以使用的剩余内存", r.freeMemory());
        result.put("JVM可以使用的处理器个数", r.availableProcessors());
        result.put("Java的运行环境版本", props.getProperty("java.version"));
        result.put("Java的运行环境供应商", props.getProperty("java.vendor"));
        result.put("Java供应商的URL", props.getProperty("java.vendor.url"));
        result.put("Java的安装路径", props.getProperty("java.home"));
        result.put("Java的虚拟机规范版本", props.getProperty("java.vm.specification.version"));
        result.put("Java的虚拟机规范供应商", props.getProperty("java.vm.specification.vendor"));
        result.put("Java的虚拟机规范名称", props.getProperty("java.vm.specification.name"));
        result.put("Java的虚拟机实现版本", props.getProperty("java.vm.version"));
        result.put("Java的虚拟机实现供应商", props.getProperty("java.vm.vendor"));
        result.put("Java的虚拟机实现名称", props.getProperty("java.vm.name"));
        result.put("Java运行时环境规范版本", props.getProperty("java.specification.version"));
        result.put("Java运行时环境规范供应商", props.getProperty("java.specification.vender"));
        result.put("Java运行时环境规范名称", props.getProperty("java.specification.name"));
        result.put("Java的类格式版本号", props.getProperty("java.class.version"));
        result.put("Java的类路径", props.getProperty("java.class.path"));
        result.put("加载库时搜索的路径列表", props.getProperty("java.library.path"));
        result.put("默认的临时文件路径", props.getProperty("java.io.tmpdir"));
        result.put("一个或多个扩展目录的路径", props.getProperty("java.ext.dirs"));
        result.put("操作系统的名称", props.getProperty("os.name"));
        result.put("操作系统的构架", props.getProperty("os.arch"));
        result.put("操作系统的版本", props.getProperty("os.version"));
        result.put("文件分隔符", props.getProperty("file.separator"));
        result.put("路径分隔符", props.getProperty("path.separator"));
        result.put("行分隔符", props.getProperty("line.separator"));
        result.put("用户的账户名称", props.getProperty("user.name"));
        result.put("用户的主目录", props.getProperty("user.home"));
        result.put("用户的当前工作目录", props.getProperty("user.dir"));
        return result;
    }

    /**
     * 获取磁盘真实使用情况
     *
     * @return 系统磁盘真实使用情况信息
     */
    public static Map disk() {
        return disk(DEFAULT_RATIO);
    }

    /**
     * 获取磁盘使用情况
     *
     * @param ratio 调整比率
     * @return 系统磁盘使用情况信息
     */
    public static Map disk(double ratio) {
        // 磁盘使用情况
        File[] files = File.listRoots();
        Map result = new HashMap(CollectionUtils.initialMapCapacity(files.length));
        for (File file : files) {
            String total = new DecimalFormat("#.#").format(file.getTotalSpace() * ratio / 1024 / 1024 / 1024) + "G";
            String free = new DecimalFormat("#.#").format(file.getFreeSpace() * ratio / 1024 / 1024 / 1024) + "G";
            String un = new DecimalFormat("#.#").format(file.getUsableSpace() * ratio / 1024 / 1024 / 1024) + "G";
            String path = file.getPath();
            Map pathMap = new HashMap(CollectionUtils.initialMapCapacity(3));
            pathMap.put("总空间", total);
            pathMap.put("可用空间", un);
            pathMap.put("空闲空间", free);
            result.put(path, pathMap);
        }
        return result;
    }

    /**
     * 获取内存真实使用情况
     *
     * @return 系统内存真实使用情况信息
     */
    public static Map memory() {
        return memory(DEFAULT_RATIO);
    }

    /**
     * 获取内存使用情况
     *
     * @param ratio 调整比率
     * @return 系统内存使用情况信息
     */
    public static Map memory(double ratio) {
        Map result = new HashMap(CollectionUtils.initialMapCapacity(6));
        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        MemoryMXBean memoryMxBean = ManagementFactory.getMemoryMXBean();
        // 堆内存使用情况
        MemoryUsage memoryUsage = memoryMxBean.getHeapMemoryUsage();
        // 初始的总内存
        long initTotalMemorySize = memoryUsage.getInit();
        // 最大可用内存
        long maxMemorySize = memoryUsage.getMax();
        // 已使用的内存
        long usedMemorySize = memoryUsage.getUsed();
        // 总的物理内存
        String totalMemorySize = new DecimalFormat("#.##").format(osmxb.getTotalPhysicalMemorySize() * ratio / 1024 / 1024 / 1024) + "G";
        // 剩余的物理内存
        String freePhysicalMemorySize = new DecimalFormat("#.##").format(osmxb.getFreePhysicalMemorySize() * ratio / 1024 / 1024 / 1024) + "G";
        // 已使用的物理内存
        String usedMemory = new DecimalFormat("#.##").format((osmxb.getTotalPhysicalMemorySize() - osmxb.getFreePhysicalMemorySize()) * ratio / 1024 / 1024 / 1024) + "G";
        result.put("总的物理内存", totalMemorySize);
        result.put("剩余的物理内存", freePhysicalMemorySize);
        result.put("已使用的物理内存", usedMemory);
        String jvmInitMem = new DecimalFormat("#.#").format(initTotalMemorySize * ratio / 1024 / 1024) + "M";
        String jvmMaxMem = new DecimalFormat("#.#").format(maxMemorySize * ratio / 1024 / 1024) + "M";
        String jvmUsedMem = new DecimalFormat("#.#").format(usedMemorySize * ratio / 1024 / 1024) + "M";
        result.put("JVM初始总内存", jvmInitMem);
        result.put("JVM最大可用内存", jvmMaxMem);
        result.put("JVM已使用的内存", jvmUsedMem);
        return result;
    }

    /**
     * 获取CPU真实使用情况
     *
     * @return CPU真实使用情况信息
     */
    public static Map cpu() {
        return cpu(DEFAULT_RATIO);
    }

    /**
     * 获取CPU使用情况
     *
     * @param ratio 调整比率
     * @return CPU使用情况信息
     */
    public static Map cpu(double ratio) {
        Map result = new HashMap(CollectionUtils.initialMapCapacity(5));
        SystemInfo systemInfo = new SystemInfo();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        // Wait 3 second...
        Util.sleep(3000);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()]
                - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()]
                - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()]
                - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()]
                - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long sys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()]
                - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()]
                - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()]
                - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()]
                - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;
        result.put("cpu核数", processor.getLogicalProcessorCount());
        result.put("cpu系统使用率", new DecimalFormat("#.##%").format(sys * ratio / totalCpu));
        result.put("cpu用户使用率", new DecimalFormat("#.##%").format(user * ratio / totalCpu));
        result.put("cpu当前等待率", new DecimalFormat("#.##%").format(iowait * ratio / totalCpu));
        result.put("cpu当前空闲率", new DecimalFormat("#.##%").format(idle * ratio / totalCpu));
        return result;
    }

}
