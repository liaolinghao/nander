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

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.tool.pageable.PageData;
import wang.bigbird.domain.framework.core.base.util.BeanUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.data.mongodb.base.util.MongoUtils;
import wang.bigbird.domain.framework.data.mongodb.service.base.IMongoDbQueryService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 查询服务
 *
 * @author Bigbird
 */
@Service
public class MongoDbQueryServiceImpl implements IMongoDbQueryService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public <T> T findById(String collection, Object id, Class<T> clazz) {
        Map<String, Object> queryMap = Collections.singletonMap("_id", id);
        List<T> list = find(collection, queryMap, clazz);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public <T> List<T> findAll(String collection, Class<T> entity) {
        return mongoTemplate.findAll(entity, collection);
    }

    @Override
    public <T> List<T> find(String collection, T queryObj) {
        Class<T> clazz = (Class<T>) queryObj.getClass();
        return find(collection, queryObj, clazz);
    }

    @Override
    public <T> List<T> find(String collection, Object queryObj, Class<T> clazz) {
        Map<String, Object> queryMap = BeanUtils.toMap(queryObj);
        return find(collection, queryMap, clazz);
    }

    @Override
    public <T> List<T> find(String collection, Map<String, Object> queryMap, Class<T> clazz) {
        return find(collection, queryMap, null, clazz);
    }

    @Override
    public <T> List<T> find(String collection, Criteria criteria, Class<T> clazz) {
        return find(collection, null, criteria, clazz);
    }

    @Override
    public <T> List<T> find(String collection, Map<String, Object> queryMap, Criteria criteria, Class<T> clazz) {
        return sortFindFilter(collection, null, queryMap, criteria, clazz, null, null);
    }

    @Override
    public <T> List<T> findFilter(String collection, List<String> fields, T queryObj) {
        Class<T> clazz = (Class<T>) queryObj.getClass();
        return findFilter(collection, fields, queryObj, clazz);
    }

    @Override
    public <T> List<T> findFilter(String collection, List<String> fields, Object queryObj, Class<T> clazz) {
        Map<String, Object> queryMap = BeanUtils.toMap(queryObj);
        return findFilter(collection, fields, queryMap, clazz);
    }

    @Override
    public <T> List<T> findFilter(String collection, List<String> fields, Map<String, Object> queryMap, Class<T> clazz) {
        return findFilter(collection, fields, queryMap, null, clazz);
    }

    @Override
    public <T> List<T> findFilter(String collection, List<String> fields, Criteria criteria, Class<T> clazz) {
        return findFilter(collection, fields, null, criteria, clazz);
    }

    @Override
    public <T> List<T> findFilter(String collection, List<String> fields, Map<String, Object> queryMap, Criteria criteria, Class<T> clazz) {
        return sortFindFilter(collection, fields, queryMap, criteria, clazz, null, null);
    }

    @Override
    public <T> List<T> sortFind(String collection, T queryObj, String sortField, Sort.Direction direction) {
        Class<T> clazz = (Class<T>) queryObj.getClass();
        return sortFind(collection, queryObj, clazz, sortField, direction);
    }

    @Override
    public <T> List<T> sortFind(String collection, Object queryObj, Class<T> clazz, String sortField, Sort.Direction direction) {
        Map<String, Object> queryMap = BeanUtils.toMap(queryObj);
        return sortFind(collection, queryMap, clazz, sortField, direction);
    }

    @Override
    public <T> List<T> sortFind(String collection, Map<String, Object> queryMap, Class<T> clazz, String sortField, Sort.Direction direction) {
        return sortFind(collection, queryMap, null, clazz, sortField, direction);
    }

    @Override
    public <T> List<T> sortFind(String collection, Criteria criteria, Class<T> clazz, String sortField, Sort.Direction direction) {
        return sortFind(collection, null, criteria, clazz, sortField, direction);
    }

    @Override
    public <T> List<T> sortFind(String collection, Map<String, Object> queryMap, Criteria criteria, Class<T> clazz, String sortField, Sort.Direction direction) {
        return sortFindFilter(collection, null, queryMap, criteria, clazz, sortField, direction);
    }

    @Override
    public <T> List<T> sortFindFilter(String collection, List<String> fields, T queryObj, String sortField, Sort.Direction direction) {
        Class<T> clazz = (Class<T>) queryObj.getClass();
        return sortFindFilter(collection, fields, queryObj, clazz, sortField, direction);
    }

    @Override
    public <T> List<T> sortFindFilter(String collection, List<String> fields, Object queryObj, Class<T> clazz, String sortField, Sort.Direction direction) {
        Map<String, Object> queryMap = BeanUtils.toMap(queryObj);
        return sortFindFilter(collection, fields, queryMap, clazz, sortField, direction);
    }

    @Override
    public <T> List<T> sortFindFilter(String collection, List<String> fields, Map<String, Object> queryMap, Class<T> clazz, String sortField, Sort.Direction direction) {
        return sortFindFilter(collection, fields, queryMap, null, clazz, sortField, direction);
    }

    @Override
    public <T> List<T> sortFindFilter(String collection, List<String> fields, Map<String, Object> queryMap, Criteria criteria, Class<T> clazz, String sortField, Sort.Direction direction) {
        return sortFindFilter(collection, fields, queryMap, criteria, clazz, sortField, direction, null);
    }

    @Override
    public <T> List<T> sortFindFilter(String collection, List<String> fields, Map<String, Object> queryMap, Criteria criteria, Class<T> clazz, String sortField, Sort.Direction direction, Boolean returnId) {
        Criteria oCriteria = MongoUtils.getCriteria(queryMap);
        Query query = (null != oCriteria) ? new Query(oCriteria) : new Query();
        if (null != criteria) {
            query.addCriteria(criteria);
        }
        if (CollectionUtils.isNotEmpty(fields)) {
            for (String field : fields) {
                query.fields().include(field);
            }
        }
        returnId = (null != returnId) ? returnId : Boolean.TRUE;
        if (!returnId) {
            query.fields().exclude("id");
        }
        if (StringUtils.isNotBlank(sortField)) {
            Sort sort = (null == direction) ? Sort.by(sortField) : Sort.by(direction, sortField);
            query.with(sort);
        }
        return mongoTemplate.find(query, clazz, collection);
    }

    @Override
    public <T> PageData<T> pageFind(String collection, T queryObj, int pageNo, int pageSize) {
        Class<T> clazz = (Class<T>) queryObj.getClass();
        return pageFind(collection, queryObj, clazz, pageNo, pageSize);
    }

    @Override
    public <T> PageData<T> pageFind(String collection, Object queryObj, Class<T> clazz, int pageNo, int pageSize) {
        Map<String, Object> queryMap = BeanUtils.toMap(queryObj);
        return pageFind(collection, queryMap, clazz, pageNo, pageSize);
    }

    @Override
    public <T> PageData<T> pageFind(String collection, Map<String, Object> queryMap, Class<T> clazz, int pageNo, int pageSize) {
        return pageFind(collection, queryMap, null, clazz, pageNo, pageSize);
    }

    @Override
    public <T> PageData<T> pageFind(String collection, Criteria criteria, Class<T> clazz, int pageNo, int pageSize) {
        return pageFind(collection, null, criteria, clazz, pageNo, pageSize);
    }

    @Override
    public <T> PageData<T> pageFind(String collection, Map<String, Object> queryMap, Criteria criteria, Class<T> clazz, int pageNo, int pageSize) {
        return pageSortFindFilter(collection, null, queryMap, criteria, clazz, null, null, pageNo, pageSize);
    }

    @Override
    public <T> PageData<T> pageSortFind(String collection, T queryObj, String sortField, Sort.Direction direction, int pageNo, int pageSize) {
        Class<T> clazz = (Class<T>) queryObj.getClass();
        return pageSortFind(collection, queryObj, clazz, sortField, direction, pageNo, pageSize);
    }

    @Override
    public <T> PageData<T> pageSortFind(String collection, Object queryObj, Class<T> clazz, String sortField, Sort.Direction direction, int pageNo, int pageSize) {
        Map<String, Object> queryMap = BeanUtils.toMap(queryObj);
        return pageSortFind(collection, queryMap, clazz, sortField, direction, pageNo, pageSize);
    }

    @Override
    public <T> PageData<T> pageSortFind(String collection, Map<String, Object> queryMap, Class<T> clazz, String sortField, Sort.Direction direction, int pageNo, int pageSize) {
        return pageSortFind(collection, queryMap, null, clazz, sortField, direction, pageNo, pageSize);
    }

    @Override
    public <T> PageData<T> pageSortFind(String collection, Criteria criteria, Class<T> clazz, String sortField, Sort.Direction direction, int pageNo, int pageSize) {
        return pageSortFind(collection, null, criteria, clazz, sortField, direction, pageNo, pageSize);
    }

    @Override
    public <T> PageData<T> pageSortFind(String collection, Map<String, Object> queryMap, Criteria criteria, Class<T> clazz, String sortField, Sort.Direction direction, int pageNo, int pageSize) {
        return pageSortFindFilter(collection, null, queryMap, criteria, clazz, sortField, direction, pageNo, pageSize);
    }

    @Override
    public <T> PageData<T> pageSortFindFilter(String collection, List<String> fields, T queryObj, String sortField, Sort.Direction direction, int pageNo, int pageSize) {
        Class<T> clazz = (Class<T>) queryObj.getClass();
        return pageSortFindFilter(collection, fields, queryObj, clazz, sortField, direction, pageNo, pageSize);
    }

    @Override
    public <T> PageData<T> pageSortFindFilter(String collection, List<String> fields, Object queryObj, Class<T> clazz, String sortField, Sort.Direction direction, int pageNo, int pageSize) {
        Map<String, Object> queryMap = BeanUtils.toMap(queryObj);
        return pageSortFindFilter(collection, fields, queryMap, clazz, sortField, direction, pageNo, pageSize);
    }

    @Override
    public <T> PageData<T> pageSortFindFilter(String collection, List<String> fields, Map<String, Object> queryMap, Class<T> clazz, String sortField, Sort.Direction direction, int pageNo, int pageSize) {
        return pageSortFindFilter(collection, fields, queryMap, null, clazz, sortField, direction, pageNo, pageSize);
    }

    @Override
    public <T> PageData<T> pageSortFindFilter(String collection, List<String> fields, Criteria criteria, Class<T> clazz, String sortField, Sort.Direction direction, int pageNo, int pageSize) {
        return pageSortFindFilter(collection, fields, null, criteria, clazz, sortField, direction, pageNo, pageSize);
    }

    @Override
    public <T> PageData<T> pageSortFindFilter(String collection, List<String> fields, Map<String, Object> queryMap, Criteria criteria, Class<T> clazz, String sortField, Sort.Direction direction, int pageNo, int pageSize) {
        Assert.isTrue(pageNo > 0, "The pageNo must be greater than 0.");
        Criteria oCriteria = MongoUtils.getCriteria(queryMap);
        Query query = (null != oCriteria) ? new Query(oCriteria) : new Query();
        if (null != criteria) {
            query.addCriteria(criteria);
        }
        if (CollectionUtils.isNotEmpty(fields)) {
            for (String field : fields) {
                query.fields().include(field);
            }
        }
        if (StringUtils.isNotBlank(sortField)) {
            Sort sort = (null == direction) ? Sort.by(sortField) : Sort.by(direction, sortField);
            query.with(sort);
        }
        long count = mongoTemplate.count(query, clazz, collection);
        int skip = pageSize * (pageNo - 1);
        query.skip(skip).limit(pageSize);
        List<T> list = mongoTemplate.find(query, clazz, collection);
        PageData<T> pageData = new PageData<>();
        pageData.setPage(pageNo);
        pageData.setPageSize(pageSize);
        pageData.setTotal(count);
        pageData.setList(list);
        return pageData;
    }

    @Override
    public long count(String collection, Object queryObj, Class<?> clazz) {
        Map<String, Object> queryMap = BeanUtils.toMap(queryObj);
        return count(collection, queryMap, clazz);
    }

    @Override
    public long count(String collection, Map<String, Object> queryMap, Class<?> clazz) {
        return count(collection, queryMap, null, clazz);
    }

    @Override
    public long count(String collection, Criteria criteria, Class<?> clazz) {
        return count(collection, null, criteria, clazz);
    }

    @Override
    public long count(String collection, Map<String, Object> queryMap, Criteria criteria, Class<?> clazz) {
        Criteria oCriteria = MongoUtils.getCriteria(queryMap);
        Query query = (null != oCriteria) ? new Query(oCriteria) : new Query();
        if (null != criteria) {
            query.addCriteria(criteria);
        }
        return (null == clazz)
                ? mongoTemplate.count(query, collection)
                : mongoTemplate.count(query, clazz, collection);
    }

}
