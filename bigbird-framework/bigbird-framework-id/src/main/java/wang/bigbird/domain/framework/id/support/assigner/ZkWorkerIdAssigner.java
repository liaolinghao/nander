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
package wang.bigbird.domain.framework.id.support.assigner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import wang.bigbird.domain.framework.core.base.tool.SystemClock;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.data.zookeeper.service.base.IZookeeperService;

import java.util.List;

/**
 * ZK编号分配器
 *
 * @author Bigbird
 */
@Slf4j
public class ZkWorkerIdAssigner extends AbstractWorkerIdAssigner {

    public static final String ZK_SPLIT = "/";

    /**
     * ZK上uid根目录
     */
    public static final String UID_ROOT = "/bigbird-uid";

    /**
     * 持久顺序节点根目录(用于保存节点的顺序编号)
     */
    public static final String UID_FOREVER = UID_ROOT.concat("/forever");

    /**
     * 临时节点根目录(用于保存活跃节点及活跃心跳)
     */
    public static final String UID_TEMPORARY = UID_ROOT.concat("/temporary");

    @Autowired
    private IZookeeperService zookeeperService;

    public ZkWorkerIdAssigner(Long interval, String pidHome, Integer pidPort) {
        super(interval, pidHome, pidPort);
    }

    @Override
    protected void initEnv() throws Exception {
        zookeeperService.createNode(UID_FOREVER, "");
        zookeeperService.createNode(UID_TEMPORARY, "");
    }

    @Override
    protected Long loadWorkerIdFromMiddleware() throws Exception {
        // 获取所有uid机器节点列表
        List<String> uidWorkers = zookeeperService.showChildren(UID_FOREVER);
        for (String uidWorker : uidWorkers) {
            if (uidWorker.startsWith(pidName)) {
                return Long.valueOf(uidWorker.substring(uidWorker.length() - 10));
            }
        }
        return null;
    }

    @Override
    protected Long assignWorkerIdByMiddleware() throws Exception {
        String workerNode = UID_FOREVER + ZK_SPLIT + pidName;
        String nodePath = zookeeperService.createSequentialNode(workerNode, String.valueOf(SystemClock.now()));
        return Long.valueOf(nodePath.substring(nodePath.length() - 10));
    }

    @Override
    protected long lastTime() throws Exception {
        String workerNodePath = UID_FOREVER + ZK_SPLIT + pidName + StringUtils.processNumberStr(10, String.valueOf(workerId));
        if (zookeeperService.checkExists(workerNodePath)) {
            return Long.parseLong(zookeeperService.getNodeData(workerNodePath));
        }
        return 0;
    }

    @Override
    protected long averageTime() {
        try {
            active.set(true);
            // 获取所有uid机器节点列表
            List<String> activeNodes = zookeeperService.showChildren(UID_TEMPORARY);
            if (CollectionUtils.isNotEmpty(activeNodes)) {
                Long sumTime = 0L;
                for (String activeNode : activeNodes) {
                    Long nodeTime = Long.valueOf(zookeeperService.getNodeData(UID_FOREVER + ZK_SPLIT + activeNode));
                    sumTime += nodeTime;
                }
                return sumTime / activeNodes.size();
            }
        } catch (Exception e) {
            log.error("AverageTime:", e);
        }
        return 0;
    }

    @Override
    protected void registerNode() throws Exception {
        String workerNodePath = UID_TEMPORARY + ZK_SPLIT + pidName + StringUtils.processNumberStr(10, String.valueOf(workerId));
        zookeeperService.createEphemeralNode(workerNodePath, "");
    }

    @Override
    protected void report() {
        try {
            String workerNodePath = UID_FOREVER + ZK_SPLIT + pidName + StringUtils.processNumberStr(10, String.valueOf(workerId));
            if (zookeeperService.checkExists(workerNodePath)) {
                zookeeperService.updateNode(workerNodePath, String.valueOf(SystemClock.now()));
            } else {
                zookeeperService.createNode(workerNodePath, String.valueOf(SystemClock.now()));
            }
        } catch (Exception e) {
            log.error("Report:", e);
        }
    }

}
