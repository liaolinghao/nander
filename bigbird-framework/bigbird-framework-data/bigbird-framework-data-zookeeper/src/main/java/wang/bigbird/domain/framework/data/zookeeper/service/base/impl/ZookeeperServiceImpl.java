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
package wang.bigbird.domain.framework.data.zookeeper.service.base.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.*;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.data.zookeeper.base.enums.AuthenticationTypeEnum;
import wang.bigbird.domain.framework.data.zookeeper.config.property.ZookeeperProperties;
import wang.bigbird.domain.framework.data.zookeeper.service.base.IZookeeperService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * zookeeper 基础服务
 *
 * @author Bigbird
 */
@Slf4j
@Service
public class ZookeeperServiceImpl implements IZookeeperService {

    @Autowired
    private ZookeeperProperties zookeeperProperties;
    @Autowired
    private CuratorFramework client;

    /**
     * 创建指定类型的节点
     * <p>
     * ZooDefs.Ids.OPEN_ACL_UNSAFE：完全开放的ACL，任何连接的客户端都可以操作该节点
     * ZooDefs.Ids.CREATOR_ALL_ACL：只有创建者才有ACL权限
     * ZooDefs.Ids.READ_ACL_UNSAFE：只能读取ACL
     *
     * @param path
     * @param value
     * @param createMode
     * @return
     * @throws Exception
     */
    private String createNode(String path, String value, CreateMode createMode) throws Exception {
        // 校验一下是否这个节点是否存在
        Stat stat = client.checkExists().forPath(path);
        if (stat == null) {
            String opResult;
            List<ACL> aclList;
            if (zookeeperProperties.getAuthentication().getType() == AuthenticationTypeEnum.world) {
                aclList = ZooDefs.Ids.OPEN_ACL_UNSAFE;
            } else {
                aclList = ZooDefs.Ids.CREATOR_ALL_ACL;
            }
            if (StringUtils.isBlank(value)) {
                // 节点数据是空的
                opResult = client.create().creatingParentsIfNeeded().withMode(createMode).withACL(aclList, true)
                        .forPath(path);
            } else {
                // 不为空就设置节点的数据值
                opResult = client.create().creatingParentsIfNeeded().withMode(createMode).withACL(aclList, true)
                        .forPath(path, value.getBytes(StandardCharsets.UTF_8));
            }
            return opResult;
        }
        return path;
    }

    @Override
    public String createNode(String path, String value) throws Exception {
        return createNode(path, value, CreateMode.PERSISTENT);
    }

    @Override
    public String createSequentialNode(String path, String value) throws Exception {
        return createNode(path, value, CreateMode.PERSISTENT_SEQUENTIAL);
    }

    @Override
    public String createEphemeralNode(String path, String value) throws Exception {
        return createNode(path, value, CreateMode.EPHEMERAL);
    }

    @Override
    public String createEphemeralSequentialNode(String path, String value) throws Exception {
        return createNode(path, value, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    @Override
    public boolean updateNode(String path, String value) throws Exception {
        // 校验一下是否这个节点是否存在
        Stat stat = client.checkExists().forPath(path);
        if (stat != null) {
            // 存在就开始更新节点数据
            Stat returnResult = client.setData().forPath(path,
                    value.getBytes(StandardCharsets.UTF_8));
            if (returnResult != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateNodeAsync(String path, String value) throws Exception {
        // 校验一下是否这个节点是否存在
        Stat stat = client.checkExists().forPath(path);
        if (stat != null) {
            // 存在就开始更新节点数据
            // 添加回调监听器，set数据成功后会对节点进行监听
            CuratorListener listener = (client, event) -> {
                log.info("Stat = {}.", JsonUtils.object2Json(event.getStat()));
                CuratorEventType eventType = event.getType();
                log.info("EventType = {}.", eventType.name());
            };
            client.getCuratorListenable().addListener(listener);
            Stat returnResult = client.setData().inBackground().forPath(path,
                    value.getBytes(StandardCharsets.UTF_8));
            if (returnResult != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean deleteNode(String path) throws Exception {
        client.delete().deletingChildrenIfNeeded().forPath(path);
        return true;
    }

    @Override
    public String getNodeData(String path) throws Exception {
        return new String(client.getData().forPath(path), StandardCharsets.UTF_8);
    }

    @Override
    public boolean checkExists(String path) throws Exception {
        Stat stat = client.checkExists().forPath(path);
        return stat != null;
    }

    @Override
    public Map<String, String> showChildrenDetail(String path) {
        try {
            GetChildrenBuilder childrenBuilder = client.getChildren();
            List<String> childrenList = childrenBuilder.forPath(path);
            GetDataBuilder dataBuilder = client.getData();
            if (CollectionUtils.isNotEmpty(childrenList)) {
                Map<String, String> nodeMap = new HashMap<>(CollectionUtils.initialMapCapacity(childrenList.size()));
                childrenList.forEach(item -> {
                    String propPath = ZKPaths.makePath(path, item);
                    try {
                        nodeMap.put(item,
                                new String(dataBuilder.forPath(propPath),
                                        StandardCharsets.UTF_8));
                    } catch (Exception e) {
                        log.error("ShowChildrenDetail:", e);
                    }
                });
                return nodeMap;
            }
        } catch (Exception e) {
            log.error("ShowChildrenDetail:", e);
        }
        return null;
    }

    @Override
    public List<String> showChildren(String path) {
        List<String> childenList = new ArrayList<>();
        try {
            GetChildrenBuilder childrenBuilder = client.getChildren();
            childenList = childrenBuilder.forPath(path);
        } catch (Exception e) {
            log.error("ShowChildren:", e);
        }
        return childenList;
    }

    @Override
    public void addWatch(String path, boolean flag) throws Exception {
        if (flag) {
            client.getData().watched().forPath(path);
        } else {
            client.getChildren().watched().forPath(path);
        }
    }

    @Override
    public void addWatch(String path, boolean flag, CuratorWatcher watcher)
            throws Exception {
        if (flag) {
            client.getData().usingWatcher(watcher).forPath(path);
        } else {
            client.getChildren().usingWatcher(watcher).forPath(path);
        }
    }

}
