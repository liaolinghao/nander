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
package wang.bigbird.domain.framework.id.base.util;

import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.core.base.util.NetUtils;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * worker id文件操作类
 *
 * @author Bigbird
 */
@Slf4j
public class WorkerIdUtils {

    /**
     * workerID文件分隔符
     */
    public static final String WORKER_SPLIT = "_";

    /**
     * 获取ip和端口构成的节点唯一标识PidName
     *
     * @param pidPort 使用端口，
     * @param socket  用于验证端口是否可用并且在节点服务时持续占用端口
     * @return 格式为：ip_port的PidName
     */
    public static String getPidName(Integer pidPort, ServerSocket socket) {
        String pidIp = NetUtils.getIp();
        if (-1 != pidPort) {
            // 占用端口，验证端口设置的合理性
            try {
                socket = new ServerSocket(pidPort);
            } catch (IOException e) {
                throw new RuntimeException("Port occupation failed!");
            }
        }
        return pidIp + WorkerIdUtils.WORKER_SPLIT + pidPort;
    }

    /**
     * 获取worker id
     * <p>
     * 如果节点对应的worker id存在，那么，在pidHome目录下会存在如下名称的文件
     * pidName_workerId
     *
     * @param pidHome workerID文件存储路径
     * @param pidName 节点唯一标识PidName
     * @return worker ID值
     */
    public static Long getWorkerId(String pidHome, String pidName) {
        String pid = null;
        File home = new File(pidHome);
        if (home.exists() && home.isDirectory()) {
            File[] files = home.listFiles();
            for (File file : files) {
                if (file.getName().startsWith(pidName)) {
                    pid = file.getName();
                    break;
                }
            }
            if (null != pid) {
                return Long.valueOf(pid.substring(pid.lastIndexOf(WORKER_SPLIT) + 1));
            }
        } else {
            home.mkdirs();
        }
        return null;
    }

    /**
     * 睡眠等待回拨时间
     *
     * @param ms   平均心跳时间
     * @param diff 回拨时间差
     */
    public static void sleepMs(long ms, long diff) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {

        }
        diff += ms;
        if (diff < 0) {
            sleepMs(ms, diff);
        }
    }

    /**
     * 创建workerID文件（workerID文件已经存在，则不创建，返回一个false；如果没有，则返回true）
     * 文件名称格式：ip_port_workerID
     *
     * @param name workerID文件路径
     */
    public static void writePidFile(String name) {
        File pidFile = new File(name);
        try {
            pidFile.createNewFile();
        } catch (IOException e) {
            log.error("WritePidFile:", e);
        }
    }

}
