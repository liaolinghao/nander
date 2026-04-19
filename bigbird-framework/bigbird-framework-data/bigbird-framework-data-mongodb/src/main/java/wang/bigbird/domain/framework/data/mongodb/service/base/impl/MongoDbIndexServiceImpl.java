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
package wang.bigbird.domain.framework.data.mongodb.service.base.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.data.mongodb.service.base.IMongoDbIndexService;

import java.util.List;
import java.util.Map;

/**
 * 索引服务
 *
 * @author Bigbird
 */
@Service
public class MongoDbIndexServiceImpl implements IMongoDbIndexService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void createIndex(String collection, String indexName, Map<String, Sort.Direction> map) {
        Assert.isFalse(map.containsKey(null),"The field added to the index cannot be null.");
        Index index = new Index().named(indexName);
        for (Map.Entry<String, Sort.Direction> entry : map.entrySet()) {
            index.on(entry.getKey(), entry.getValue());
        }
        mongoTemplate.indexOps(collection).ensureIndex(index);
    }

    @Override
    public List<IndexInfo> getIndexInfo(String collection) {
        return mongoTemplate.indexOps(collection).getIndexInfo();
    }

    @Override
    public void removeIndexByName(String collection, String indexName) {
        mongoTemplate.indexOps(collection).dropIndex(indexName);
    }

    @Override
    public void removeAllIndex(String collection) {
        mongoTemplate.indexOps(collection).dropAllIndexes();
    }

}
