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

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.data.mongodb.base.util.MongoUtils;
import wang.bigbird.domain.framework.data.mongodb.service.base.IMongoDbManipulateService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据操作服务
 *
 * @author Bigbird
 */
@Service
public class MongoDbManipulateServiceImpl implements IMongoDbManipulateService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public <T> void save(String collection, T entity) {
        mongoTemplate.save(entity, collection);
    }

    @Override
    public <T> void saveAll(String collection, List<T> entities) {
        for (Object entity : entities) {
            save(collection, entity);
        }
    }

    @Override
    public boolean updateById(String collection, Object id, Map<String, Object> updateMap) {
        Map<String, Object> queryMap = Collections.singletonMap("_id", id);
        return update(collection, queryMap, updateMap, true) > 0L;
    }

    @Override
    public boolean updateOne(String collection, Map<String, Object> queryMap, Map<String, Object> updateMap) {
        return update(collection, queryMap, updateMap, true) > 0L;
    }

    @Override
    public long updateAll(String collection, Map<String, Object> queryMap, Map<String, Object> updateMap) {
        return update(collection, queryMap, updateMap, false);
    }

    @Override
    public long update(String collection, Map<String, Object> queryMap, Map<String, Object> updateMap, boolean isOnlyFirst) {
        Criteria criteria = MongoUtils.getCriteria(queryMap);
        Query query = (null != criteria) ? new Query(criteria) : new Query();
        Update update = new Update();
        if (MapUtils.isNotEmpty(updateMap)) {
            for (Map.Entry<String, Object> entry : updateMap.entrySet()) {
                if (StringUtils.isBlank(entry.getKey())) {
                    continue;
                }
                update.set(entry.getKey(), entry.getValue());
            }
        }
        if (isOnlyFirst) {
            return mongoTemplate.updateFirst(query, update, collection).getModifiedCount();
        } else {
            return mongoTemplate.updateMulti(query, update, collection).getModifiedCount();
        }
    }

    @Override
    public long removeById(String collection, Object id) {
        Map<String, Object> queryMap = Collections.singletonMap("_id", id);
        return remove(collection, queryMap);
    }

    @Override
    public long remove(String collection, String key, Object value) {
        Map<String, Object> map = new HashMap<>(CollectionUtils.initialMapCapacity(1));
        map.put(key, value);
        return remove(collection, map);
    }

    @Override
    public long remove(String collection, Map<String, Object> queryMap) {
        return remove(collection, queryMap, null);
    }

    @Override
    public long remove(String collection, Map<String, Object> queryMap, Criteria criteria) {
        Criteria oCriteria = MongoUtils.getCriteria(queryMap);
        Query query = (null != oCriteria) ? new Query(oCriteria) : new Query();
        if (null != criteria) {
            query.addCriteria(criteria);
        }
        return mongoTemplate.remove(query, collection).getDeletedCount();
    }

}
