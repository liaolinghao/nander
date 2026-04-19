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
package wang.bigbird.domain.framework.server.web.core.base.tool;


import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * 将uri以树的形式存储起来
 *
 * @author Bigbird
 */
public final class UriTree {

    /**
     * 通配符
     */
    private static final String ANY = "**";

    private final Node anyNode = new Node(ANY);

    /**
     * 树的根节点
     */
    private Node root;

    /**
     * 树的节点
     */
    private static class Node {
        /**
         * 节点存储的内容
         */
        String chips;
        /**
         * 是否结束，表示该节点是否是某一个URL的最后一个片段
         */
        boolean isEnd = false;
        /**
         * 该节点的儿子节点
         */
        Node child = null;
        /**
         * 该节点的兄弟节点
         */
        Node brother = null;

        Node(String chips) {
            this.chips = chips;
        }
    }

    public UriTree() {
        root = new Node("root");
    }

    /**
     * 添加uri到树中，添加算法与DFA算法有一定的类似性
     *
     * 可把uri理解为一个树枝，以/分隔的每一个片段理解为树枝上的一个树芽
     * 但是如果uri中含有**符号，树枝会自动剪枝，包含两种情况：
     * 1、添加的uri包含**，只认可从开头到**结尾的一部分
     * 2、添加的uri不包含**，但是存在相同模式的前一个uri包含**，那么该uri将不会被成功添加
     *
     * @param uri uri信息
     * @return
     */
    private void add(String uri) {
        if (StringUtils.isBlank(uri)) {
            return;
        }
        uri = uri.trim();
        String[] split = uri.split(CommonConstants.SLASH);
        Node c = root;
        for (String s : split) {
            if (ANY.equals(s)) {
                // 当前片段表示任意值，则不继续处理，即剪枝
                c.child = anyNode;
                return;
            } else {
                if (Objects.isNull(c.child)) {
                    // 当前节点的子节点为空，那么把当前片段构造的节点作为该节点的子节点
                    c.child = new Node(s);
                    // 递归衔接下一个节点
                    c = c.child;
                } else if (ANY.equals(c.child.chips)) {
                    // 当前节点的子节点保存为任意值，则不进行处理，即剪枝
                    return;
                } else {
                    // 遍历该节点下的子节点集合，
                    // 通过子节点与子节点的兄弟节点完成遍历搜索，查找是否存在信息相同的节点
                    c = c.child;
                    while (!c.chips.equals(s) && c.brother != null) {
                        c = c.brother;
                    }
                    // 如果信息相同，那么不做处理，信息不同，创造新的最后出生的兄弟节点
                    if (!c.chips.equals(s)) {
                        c.brother = new Node(s);
                        c = c.brother;
                    }
                }
            }
        }
        c.isEnd = true;
    }

    /**
     * 判断uri在该树中
     *
     * @param uri uri信息
     * @return boolean
     */
    public boolean contains(String uri) {
        if (root.child == null || StringUtils.isBlank(uri)) {
            return false;
        }
        uri = uri.trim();
        String[] split = uri.split(CommonConstants.SLASH);
        Node c = root;
        for (String s : split) {
            if (c.child == null) {
                return false;
            } else {
                if (ANY.equals(c.child.chips)) {
                    return true;
                } else {
                    c = c.child;
                    while (!c.chips.equals(s) && c.brother != null) {
                        c = c.brother;
                    }
                    if (!c.chips.equals(s)) {
                        return false;
                    }
                }
            }
        }
        return c.child == null || c.child.chips.equals(ANY) || c.isEnd;
    }

    /**
     * 通过uriList构造UriTree
     *
     * @param uriList uri信息列表
     * @return UriTree
     */
    public static UriTree create(List<String> uriList) {
        UriTree uriTree = new UriTree();
        uriList.forEach(uriTree::add);
        return uriTree;
    }

}
