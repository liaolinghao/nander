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

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.exception.ProcessingRuntimeException;
import wang.bigbird.domain.framework.data.elasticsearch.service.base.IEsIndexService;

import java.io.IOException;

/**
 * 索引服务
 *
 * @author Bigbird
 */
@Service
public class EsIndexServiceImpl implements IEsIndexService {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private RestHighLevelClient restHighLevelClient;


    @Override
    public <T> boolean createIndex(Class<T> clazz) {
        return elasticsearchRestTemplate.indexOps(clazz).create();
    }

    @Override
    public boolean createIndex(CreateIndexRequest request) {
        CreateIndexResponse createIndexResponse;
        try {
            createIndexResponse = restHighLevelClient.indices()
                    .create(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ProcessingRuntimeException(e);
        }
        return createIndexResponse.isAcknowledged();
    }

    @Override
    public <T> boolean deleteIndex(Class<T> clazz) {
        return elasticsearchRestTemplate.indexOps(clazz).delete();
    }

    @Override
    public boolean deleteIndex(String indexName) {
        return elasticsearchRestTemplate.indexOps(IndexCoordinates.of(new String[]{indexName})).delete();
    }

    @Override
    public boolean existsIndex(String indexName) {
        GetIndexRequest request = new GetIndexRequest(indexName);
        try {
            return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ProcessingRuntimeException(e);
        }
    }
}
