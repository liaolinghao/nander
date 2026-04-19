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
package wang.bigbird.domain.framework.data.zookeeper.service.base;

import org.apache.curator.framework.api.CuratorWatcher;

import java.util.List;
import java.util.Map;

/**
 * zookeeper 基础服务
 *
 * @author Bigbird
 */
public interface IZookeeperService {

    /**
     * 创建持久节点
     * session断联、服务端重启还在；
     * 可以创建子节点，子节点可以临时也可以持久；
     * 不能同名；
     *
     * @param path  节点路径
     * @param value 节点数据
     * @return 真实创建的节点路径
     * @throws Exception
     */
    String createNode(String path, String value) throws Exception;

    /**
     * 创建持久有序节点
     * session断联、服务端重启还在；
     * 可以创建子节点，子节点可以临时也可以持久；
     * 同名节点会在后面添加上序号；
     *
     * @param path  节点路径
     * @param value 节点数据
     * @return 真实创建的节点路径，带着节点序号
     * @throws Exception
     */
    String createSequentialNode(String path, String value) throws Exception;

    /**
     * 创建临时节点
     * session链接断开就没了；
     * 不能创建子节点；
     * 不能同名；
     *
     * @param path  节点路径
     * @param value 节点数据
     * @return 真实创建的节点路径
     * @throws Exception
     */
    String createEphemeralNode(String path, String value) throws Exception;

    /**
     * 创建临时有序节点
     * session链接断开就没了；
     * 不能创建子节点；
     * 同名节点会在后面添加上序号（分布式锁使用的好处）；
     *
     * @param path  节点路径
     * @param value 节点数据
     * @return 真实创建的节点路径，带着节点序号
     * @throws Exception
     */
    String createEphemeralSequentialNode(String path, String value) throws Exception;

    /**
     * 更新节点
     *
     * @param path
     * @param value
     * @return 更新是否成功
     * @throws Exception
     */
    boolean updateNode(String path, String value) throws Exception;

    /**
     * 异步更新节点
     *
     * @param path
     * @param value
     * @return 更新是否成功
     * @throws Exception
     */
    boolean updateNodeAsync(String path, String value) throws Exception;

    /**
     * 删除节点
     *
     * @param path
     * @return 删除是否成功
     * @throws Exception
     */
    boolean deleteNode(String path) throws Exception;

    /**
     * 获取节点数据
     *
     * @param path 节点路径
     * @return 节点数据
     * @throws Exception
     */
    String getNodeData(String path) throws Exception;

    /**
     * 判断节点是否存在
     *
     * @param path 节点路径
     * @return 节点是否存在
     * @throws Exception
     */
    boolean checkExists(String path) throws Exception;

    /**
     * 获取指定节点下的所有子节点的名称与值
     *
     * @param path 节点路径
     * @return 所有子节点的名称与值
     */
    Map<String, String> showChildrenDetail(String path);

    /**
     * 列出节点下所有的子节点，但是不带子节点的数据
     *
     * @param path 节点路径
     * @return 子节点名称
     */
    List<String> showChildren(String path);

    /**
     * 对节点增加监听
     *
     * @param path
     * @param flag 如果是true，就对节点本身监听，如果是false，就对该节点的子节点增加监听
     * @throws Exception
     */
    void addWatch(String path, boolean flag) throws Exception;

    /**
     * 对节点增加监听
     *
     * @param path
     * @param flag    如果是true，就对节点本身监听，如果是false，就对该节点的子节点增加监听
     * @param watcher 监视器
     * @throws Exception
     */
    void addWatch(String path, boolean flag, CuratorWatcher watcher)
            throws Exception;


}
