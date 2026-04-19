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
package wang.bigbird.domain.framework.data.mongodb.service.base;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.index.IndexInfo;

import java.util.List;
import java.util.Map;

/**
 * 索引服务
 *
 * @author Bigbird
 */
public interface IMongoDbIndexService {

    /**
     * 向指定集合设置索引
     *
     * @param collection 集合名称
     * @param indexName  索引名称
     * @param map        map.put("添加索引的字段",Direction.ASC/DESC)
     */
    void createIndex(String collection, String indexName, Map<String, Sort.Direction> map);

    /**
     * 获取指定集合中的索引信息
     *
     * @param collection 集合名称
     * @return 索引信息集合
     */
    List<IndexInfo> getIndexInfo(String collection);

    /**
     * 根据索引名称删除指定集合的索引
     *
     * @param collection 集合名称
     * @param indexName  索引名称
     */
    void removeIndexByName(String collection, String indexName);

    /**
     * 删除指定集合中的所有索引
     *
     * @param collection 集合名称
     */
    void removeAllIndex(String collection);

}
