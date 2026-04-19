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

import wang.bigbird.domain.framework.data.elasticsearch.domain.entity.BaseEntity;

import java.util.List;

/**
 * 批量操作服务
 *
 * @author Bigbird
 */
public interface IEsBulkService {

    /**
     * 异步批量插入
     *
     * @param index      索引名
     * @param type       索引类型
     * @param entityList 实体列表
     */
    <T> void bulkAddAsync(String index, String type, List<T> entityList);

    /**
     * 同步批量插入
     *
     * @param index      索引名
     * @param type       索引类型
     * @param entityList 实体列表
     */
    <T> void bulkAddSync(String index, String type, List<T> entityList);

    /**
     * 异步批量更新
     *
     * @param index      索引名
     * @param type       索引类型
     * @param entityList 实体列表
     */
    <T extends BaseEntity> void bulkUpdateAsync(String index, String type, List<T> entityList);

    /**
     * 同步批量更新
     *
     * @param index      索引名
     * @param type       索引类型
     * @param entityList 实体列表
     */
    <T extends BaseEntity> void bulkUpdateSync(String index, String type, List<T> entityList);

    /**
     * 异步批量删除
     *
     * @param index  索引名
     * @param type   索引类型
     * @param idList id列表
     */
    void bulkDeleteAsync(String index, String type, List<String> idList);

    /**
     * 同步批量删除
     *
     * @param index  索引名
     * @param type   索引类型
     * @param idList id列表
     */
    void bulkDeleteSync(String index, String type, List<String> idList);

}
