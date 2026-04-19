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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 一致性Hash，解决分布式系统中负载均衡的问题
 *
 * @param <T>
 * @param <K>
 * @author Bigbird
 */
public class ConsistentHash<T, K> {

    /**
     * 虚拟节点集合
     */
    private final TreeMap<Long, T> virtualNodeMap;

    /**
     * 为每个真实节点生成的虚拟节点个数
     */
    private final int virtualNumberPerNode = 200;

    /**
     * 每四个虚拟节点为一组，对应MD5哈希的4个字节
     */
    private static final int VIRTUAL_NODE_GROUP_SIZE = 4;

    public ConsistentHash(List<T> trueNodes) {
        virtualNodeMap = new TreeMap<>();
        buildHashRing(trueNodes);
    }

    /**
     * 返回为指定key服务的节点
     *
     * @param selectKey 指定key
     * @return 服务节点
     */
    public T select(K selectKey) {
        byte[] digest = md5(selectKey.toString());
        T trueNode = selectForKey(hash(digest, 0));
        return trueNode;
    }

    /**
     * 获取虚拟节点数
     *
     * @return
     */
    public int getVirtualNodeNum() {
        return virtualNodeMap.size();
    }

    /**
     * 返回环上距离hash值最近的key对应的真实节点
     *
     * @param hash
     * @return 距离hash值最近的key对应的真实节点
     */
    private T selectForKey(long hash) {
        T trueNode;
        Long key = hash;
        if (!virtualNodeMap.containsKey(key)) {
            // 返回大于或等于给定hash值的的有序子集
            SortedMap<Long, T> tailMap = virtualNodeMap.tailMap(key);
            if (tailMap.isEmpty()) {
                // 如果子集为空，则返回起点（故名：Hash环）
                key = virtualNodeMap.firstKey();
            } else {
                // 返回大于或等于Key，最接近给定hash值的环上的key
                key = tailMap.firstKey();
            }
        }
        // 如果hash值正好是Hash环上的节点key
        trueNode = virtualNodeMap.get(key);
        return trueNode;
    }

    /**
     * 根据真实节点生成虚拟节点，构建Hash环
     *
     * @param trueNodes
     */
    private void buildHashRing(List<T> trueNodes) {
        for (T trueNode : trueNodes) {
            for (int i = 0; i < virtualNumberPerNode / VIRTUAL_NODE_GROUP_SIZE; i++) {
                // 每四个虚拟结点为一组
                byte[] digest = md5(getVirtualKeyForNode(trueNode, i));
                for (int h = 0; h < VIRTUAL_NODE_GROUP_SIZE; h++) {
                    // 对于每四个字节，组成一个long值数值，做为这个虚拟节点的在环中的惟一key
                    Long virtualKey = hash(digest, h);
                    // 所有virtualNumberPerNode个虚拟节点key都指向trueNode
                    virtualNodeMap.put(virtualKey, trueNode);
                }
            }
        }
    }

    /**
     * 根据真实节点，生成虚拟节点Key
     *
     * @param node
     * @param i
     * @return
     */
    private String getVirtualKeyForNode(T node, int i) {
        return "Node-" + node + "-virtual:" + i;
    }

    /**
     * 对虚拟节点名称做MD5，Md5是一个16字节长度的数组
     *
     * @param virtualKey
     * @return
     */
    private byte[] md5(String virtualKey) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        md5.reset();
        byte[] bytes = null;
        try {
            bytes = virtualKey.getBytes(Coder.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage(), e);

        }
        md5.update(bytes);
        return md5.digest();
    }

    /**
     * 将MD5返回的16字节的数组分为四组，每四个字节一组，组成一个long值数值，做为虚拟节点在环中的惟一key
     *
     * @param digest
     * @param number
     * @return
     */
    private long hash(byte[] digest, int number) {
        return (((long) (digest[3 + number * 4] & 0xFF) << 24)
                | ((long) (digest[2 + number * 4] & 0xFF) << 16)
                | ((long) (digest[1 + number * 4] & 0xFF) << 8) | (digest[0 + number * 4] & 0xFF)) & 0xFFFFFFFFL;
    }

}
