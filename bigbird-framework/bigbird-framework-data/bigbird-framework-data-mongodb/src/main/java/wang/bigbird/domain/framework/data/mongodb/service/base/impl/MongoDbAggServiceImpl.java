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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.data.mongodb.service.base.IMongoDbAggService;
import wang.bigbird.domain.framework.data.mongodb.base.util.MongoUtils;

import java.util.List;
import java.util.Map;

/**
 * 聚合服务
 *
 * @author Bigbird
 */
@Service
public class MongoDbAggServiceImpl implements IMongoDbAggService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public <T> List<T> sum(String collection, Map<String, Object> queryMap, Class<T> clazz, String sumField) {
        return sum(collection, queryMap, clazz, sumField, null);
    }

    @Override
    public <T> List<T> sum(String collection, Map<String, Object> queryMap, Class<T> clazz, String sumField, String... groupFields) {
        return sum(collection, queryMap, null, clazz, sumField, groupFields);
    }

    @Override
    public <T> List<T> sum(String collection, Map<String, Object> queryMap, Criteria criteria, Class<T> clazz, String sumField, String... groupFields) {
        Criteria oCriteria = MongoUtils.getCriteria(queryMap);
        if (null != criteria) {
            oCriteria.andOperator(criteria);
        }
        MatchOperation match = (null != oCriteria) ? Aggregation.match(oCriteria) : null;
        GroupOperation count = (null != groupFields && groupFields.length > 0) ? Aggregation.group(groupFields).sum(sumField).as(sumField) : Aggregation.group().sum(sumField).as(sumField);
        return mongoTemplate.aggregate(Aggregation.newAggregation(match, count), collection, clazz).getMappedResults();
    }

}
