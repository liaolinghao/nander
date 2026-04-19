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

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.core.exception.ProcessingRuntimeException;
import wang.bigbird.domain.framework.data.elasticsearch.service.base.IEsManipulateService;

import java.io.IOException;

/**
 * 数据操作服务
 *
 * @author Bigbird
 */
@Service
public class EsManipulateServiceImpl implements IEsManipulateService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public String insert(String index, String type, String id, Object entity) {
        try {
            IndexRequest indexRequest;
            if (StringUtils.isNotBlank(id)) {
                indexRequest = new IndexRequest(index, type, id);
            } else {
                indexRequest = new IndexRequest(index, type);
            }
            String serialize = JsonUtils.object2Json(entity);
            indexRequest.source(serialize, XContentType.JSON);
            IndexResponse indexResponse = restHighLevelClient
                    .index(indexRequest, RequestOptions.DEFAULT);
            return indexResponse.getId();
        } catch (IOException e) {
            throw new ProcessingRuntimeException(e);
        }
    }

    @Override
    public boolean update(String index, String type, String id, Object entity) {
        try {
            UpdateRequest request = new UpdateRequest(index, type, id);
            String serialize = JsonUtils.object2Json(entity);
            request.doc(serialize, XContentType.JSON);
            UpdateResponse response = restHighLevelClient.update(request, RequestOptions.DEFAULT);
            return response.getResult() != DocWriteResponse.Result.NOOP && response.getResult() != DocWriteResponse.Result.NOT_FOUND;
        } catch (IOException e) {
            throw new ProcessingRuntimeException(e);
        }
    }

    @Override
    public boolean delete(String index, String type, String id) {
        try {
            DeleteRequest request = new DeleteRequest(index, type, id);
            DeleteResponse response = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
            return response.getResult() != DocWriteResponse.Result.NOOP && response.getResult() != DocWriteResponse.Result.NOT_FOUND;
        } catch (IOException e) {
            throw new ProcessingRuntimeException(e);
        }
    }
}
