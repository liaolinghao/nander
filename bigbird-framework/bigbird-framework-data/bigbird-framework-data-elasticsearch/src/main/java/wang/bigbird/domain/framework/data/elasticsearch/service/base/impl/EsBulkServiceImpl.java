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
package wang.bigbird.domain.framework.data.elasticsearch.service.base.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.exception.ProcessingRuntimeException;
import wang.bigbird.domain.framework.data.elasticsearch.domain.entity.BaseEntity;
import wang.bigbird.domain.framework.data.elasticsearch.service.base.IEsBulkService;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

/**
 * 批量操作服务
 *
 * @author Bigbird
 */
@Slf4j
@Service
public class EsBulkServiceImpl implements IEsBulkService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private BulkProcessor bulkProcessor;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        MAPPER.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @Override
    public <T> void bulkAddAsync(String index, String type, List<T> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return;
        }
        entityList.forEach(entity -> {
            IndexRequest indexRequest;
            String primaryId = getPrimaryId(entity);
            if (StringUtils.isNotBlank(primaryId)) {
                indexRequest = new IndexRequest(index, type, primaryId);
            } else {
                indexRequest = new IndexRequest(index, type);
            }
            try {
                indexRequest.source(MAPPER.writeValueAsString(entity), XContentType.JSON);
            } catch (JsonProcessingException e) {
                throw new ProcessingRuntimeException(e, "bulk add io exception, index:{}, type:{}", index, type);
            }
            bulkProcessor.add(indexRequest);
        });
    }

    @Override
    public <T> void bulkAddSync(String index, String type, List<T> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return;
        }
        try {
            BulkRequest bulkRequest = new BulkRequest();
            for (T entity : entityList) {
                IndexRequest indexRequest;
                String primaryId = getPrimaryId(entity);
                if (StringUtils.isNotBlank(primaryId)) {
                    indexRequest = new IndexRequest(index, type, primaryId);
                } else {
                    indexRequest = new IndexRequest(index, type);
                }
                indexRequest.source(MAPPER.writeValueAsString(entity), XContentType.JSON);
                bulkRequest.add(indexRequest);
            }
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            if (bulkResponse.hasFailures()) {
                throw new ProcessingRuntimeException("bulk add exception, index:{}, type:{}, message: {}", index, type, bulkResponse.buildFailureMessage());
            }
        } catch (IOException e) {
            throw new ProcessingRuntimeException(e, "bulk add io exception, index:{}, type:{}", index, type);
        }
    }

    @Override
    public <T extends BaseEntity> void bulkUpdateAsync(String index, String type, List<T> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return;
        }
        entityList.forEach(entity -> {
            String primaryId = getPrimaryId(entity);
            UpdateRequest request = new UpdateRequest(index, type, primaryId);
            try {
                request.doc(MAPPER.writeValueAsString(entity), XContentType.JSON);
            } catch (JsonProcessingException e) {
                throw new ProcessingRuntimeException(e, "bulk update io exception, index:{}, type:{}", index, type);
            }
            bulkProcessor.add(request);
        });
    }

    @Override
    public <T extends BaseEntity> void bulkUpdateSync(String index, String type, List<T> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return;
        }
        try {
            BulkRequest bulkRequest = new BulkRequest();
            for (BaseEntity entity : entityList) {
                String primaryId = getPrimaryId(entity);
                UpdateRequest updateRequest = new UpdateRequest(index, type, primaryId);
                updateRequest.doc(MAPPER.writeValueAsString(entity), XContentType.JSON);
                bulkRequest.add(updateRequest);
            }
            BulkResponse bulkResponse = restHighLevelClient
                    .bulk(bulkRequest, RequestOptions.DEFAULT);
            if (bulkResponse.hasFailures()) {
                throw new ProcessingRuntimeException("bulk update exception, index:{}, type:{}, message: {}", index, type, bulkResponse.buildFailureMessage());
            }
        } catch (IOException e) {
            throw new ProcessingRuntimeException(e, "bulk update io exception, index:{}, type:{}", index, type);
        }
    }

    @Override
    public void bulkDeleteAsync(String index, String type, List<String> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }
        idList.forEach(id -> {
            DeleteRequest request = new DeleteRequest(index, type, id);
            bulkProcessor.add(request);
        });
    }

    @Override
    public void bulkDeleteSync(String index, String type, List<String> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }
        try {
            BulkRequest bulkRequest = new BulkRequest();
            for (String id : idList) {
                DeleteRequest deleteRequest = new DeleteRequest(index, type, id);
                bulkRequest.add(deleteRequest);
            }
            BulkResponse bulkResponse = restHighLevelClient
                    .bulk(bulkRequest, RequestOptions.DEFAULT);
            if (bulkResponse.hasFailures()) {
                throw new ProcessingRuntimeException("bulk delete exception, index:{}, type:{}, message: {}", index, type, bulkResponse.buildFailureMessage());
            }
        } catch (IOException e) {
            throw new ProcessingRuntimeException(e, "bulk delete io exception, index:{}, type:{}", index, type);
        }
    }

    /**
     * 获取主键
     * 优先获取 primaryId 的值。
     * 如果没有实现 EsBaseEntity 接口，则获取 @Id 的值
     *
     * @param entity 数据实体
     * @return 主键值
     */
    private String getPrimaryId(Object entity) {
        if (entity instanceof BaseEntity) {
            return ((BaseEntity) entity).getPrimaryId();
        }
        try {
            Class<?> clazz = entity.getClass();
            Field field = ReflectionUtils.findField(clazz, new ReflectionUtils.AnnotationFieldFilter(Id.class));
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            Object idObj = field.get(entity);
            if (null == idObj) {
                return null;
            }
            return idObj.toString();
        } catch (Exception e) {
            return null;
        }
    }

}
