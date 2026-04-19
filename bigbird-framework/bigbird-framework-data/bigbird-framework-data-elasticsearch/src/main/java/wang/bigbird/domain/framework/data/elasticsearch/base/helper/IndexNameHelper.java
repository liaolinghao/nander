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
package wang.bigbird.domain.framework.data.elasticsearch.base.helper;

import wang.bigbird.domain.framework.core.base.tool.ConsistentHash;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 索引名称辅助类，提供索引名称的映射方法
 *
 * @author Bigbird
 */
public class IndexNameHelper {

    /**
     * 索引拆分
     */
    private static List<String> nodes = Arrays.asList("_0", "_1", "_2", "_3", "_4", "_5", "_6", "_7", "_8", "_9");

    /**
     * 按照分区标识selectKey和索引名称前缀获取路由到的索引名称
     *
     * @param selectKey    分区标识
     * @param preIndexName 索引名称前缀
     * @return 路由到的索引名称
     */
    public static String getIndexName(String selectKey, String preIndexName) {
        ConsistentHash<String, String> cHash = new ConsistentHash<>(nodes);
        String suffix = cHash.select(selectKey);
        return StringUtils.joinStr(preIndexName, suffix);
    }

}
