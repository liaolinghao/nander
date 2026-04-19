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

/**
 * 数据操作服务
 *
 * @author Bigbird
 */
public interface IEsManipulateService {

    /**
     * 插入单条
     *
     * @param index  索引
     * @param type   索引类型
     * @param id     主键
     * @param entity 实体对象
     * @return 主键
     */
    String insert(String index, String type, String id, Object entity);


    /**
     * 更新单条
     *
     * @param index  索引
     * @param type   索引类型
     * @param id     主键
     * @param entity 实体对象
     * @return 是否更新成功
     */
    boolean update(String index, String type, String id, Object entity);


    /**
     * 删除单条
     *
     * @param index 索引
     * @param type  索引类型
     * @param id    主键
     * @return 是否删除成功
     */
    boolean delete(String index, String type, String id);
}
