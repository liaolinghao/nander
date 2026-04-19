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
package wang.bigbird.domain.framework.data.elasticsearch.service.base;

import org.elasticsearch.client.indices.CreateIndexRequest;

/**
 * 索引服务
 *
 * @author Bigbird
 */
public interface IEsIndexService {

    /**
     * 创建索引
     *
     * @param clazz
     *
     * 注解使用请参照 spring-data-elasticsearch
     * org.springframework.data.annotation.Id;
     * org.springframework.data.elasticsearch.annotations.DateFormat;
     * org.springframework.data.elasticsearch.annotations.Document;
     * org.springframework.data.elasticsearch.annotations.Field;
     * org.springframework.data.elasticsearch.annotations.FieldType;
     *
     * @return 是否成功
     */
    <T> boolean createIndex(Class<T> clazz);

    /**
     * 创建索引
     *
     * @param request 索引参数
     * @return 是否成功
     */
    boolean createIndex(CreateIndexRequest request);

    /**
     * 删除索引
     *
     * @param clazz
     *
     * 注解使用请参照 spring-data-elasticsearch
     * org.springframework.data.annotation.Id;
     * org.springframework.data.elasticsearch.annotations.DateFormat;
     * org.springframework.data.elasticsearch.annotations.Document;
     * org.springframework.data.elasticsearch.annotations.Field;
     * org.springframework.data.elasticsearch.annotations.FieldType;
     *
     * @return 是否成功
     */
    <T> boolean deleteIndex(Class<T> clazz);

    /**
     * 删除
     *
     * @param index 索引名
     * @return 是否成功
     */
    boolean deleteIndex(String index);

    /**
     * 索引是否存在
     *
     * @param index 索引名
     * @return 是否存在
     */
    boolean existsIndex(String index);
}
