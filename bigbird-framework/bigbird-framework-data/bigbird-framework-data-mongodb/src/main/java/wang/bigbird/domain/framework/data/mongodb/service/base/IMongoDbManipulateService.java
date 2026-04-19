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
package wang.bigbird.domain.framework.data.mongodb.service.base;

import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;
import java.util.Map;

/**
 * 数据操作服务
 *
 * @author Bigbird
 */
public interface IMongoDbManipulateService {

    /**
     * 保存单个对象到指定集合中
     *
     * @param collection 集合名称
     * @param entity     实体名称
     */
    <T> void save(String collection, T entity);

    /**
     * 保存对象List到指定集合中
     *
     * @param collection 集合名称
     * @param entities
     */
    <T> void saveAll(String collection, List<T> entities);

    /**
     * 修改指定集合指定主键的数据
     *
     * @param collection 集合名称
     * @param id         主键
     * @param updateMap  Map<修改内容 key数组,修改内容 value数组>
     * @return 是否修改成功
     */
    boolean updateById(String collection, Object id, Map<String, Object> updateMap);

    /**
     * 修改指定集合符合条件的第一条数据
     *
     * @param collection 集合名称
     * @param queryMap   Map<查询条件key,查询条件value>
     * @param updateMap  Map<修改内容 key数组,修改内容 value数组>
     * @return 是否修改成功
     */
    boolean updateOne(String collection, Map<String, Object> queryMap, Map<String, Object> updateMap);

    /**
     * 修改指定集合符合条件的所有数据
     *
     * @param collection 集合名称
     * @param queryMap   Map<查询条件key,查询条件value>
     * @param updateMap  Map<修改内容 key数组,修改内容 value数组>
     * @return 修改数量
     */
    long updateAll(String collection, Map<String, Object> queryMap, Map<String, Object> updateMap);

    /**
     * 指定集合 修改数据，且修改所找到的数据（一条/多条）
     *
     * @param collection  集合名称
     * @param queryMap    Map<查询条件key,查询条件value>
     * @param updateMap   Map<修改内容 key数组,修改内容 value数组>
     * @param isOnlyFirst 修改操作类型  true：修改第一条数据  false：修改所有匹配得数据
     * @return 修改数量
     */
    long update(String collection, Map<String, Object> queryMap, Map<String, Object> updateMap, boolean isOnlyFirst);

    /**
     * 删除指定集合指定主键的数据
     *
     * @param collection 集合名称
     * @param id         主键
     * @return 删除数量
     */
    long removeById(String collection, Object id);

    /**
     * 根据指定key和value到指定集合中删除数据
     *
     * @param collection 集合名称
     * @param key        查询条件key
     * @param value      查询条件value
     * @return 删除数量
     */
    long remove(String collection, String key, Object value);

    /**
     * 根据指定key和value构成的Map，到指定集合中删除数据
     *
     * @param collection 集合名称
     * @param queryMap   Map<查询条件key,查询条件value>
     * @return 删除数量
     */
    long remove(String collection, Map<String, Object> queryMap);

    /**
     * 根据指定key和value到指定collName集合中删除数据
     *
     * @param collection 集合名称
     * @param queryMap   Map<查询条件key,查询条件value>
     * @param criteria   示例： lt小于  lte 小于等于  gt大于  gte大于等于 eq等于 ne不等于
     *                   <p>
     *                   Criteria criteria=Criteria.where("createDate").gte(begin).lte(end);
     *                   <p>
     * @return 删除数量
     */
    long remove(String collection, Map<String, Object> queryMap, Criteria criteria);

}
