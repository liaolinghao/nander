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
 * 聚合服务
 *
 * @author Bigbird
 */
public interface IMongoDbAggService {

    /**
     * 对某字段做sum求和
     *
     * @param collection 集合名称
     * @param queryMap   Map<查询条件key,查询条件value> map
     * @param clazz      数据实体类
     * @param sumField   求和字段
     * @return 分组求和后的数据集合
     */
    <T> List<T> sum(String collection, Map<String, Object> queryMap, Class<T> clazz, String sumField);


    /**
     * 对某字段分组做sum求和
     *
     * @param collection  集合名称
     * @param queryMap    Map<查询条件key,查询条件value> map
     * @param clazz       数据实体类
     * @param sumField    求和字段
     * @param groupFields 分组字段
     * @return 分组求和后的数据集合
     */
    <T> List<T> sum(String collection, Map<String, Object> queryMap, Class<T> clazz, String sumField, String... groupFields);


    /**
     * 对某字段分组做sum求和
     *
     * @param collection  集合名称
     * @param queryMap    Map<查询条件key,查询条件value> map
     * @param criteria    示例： lt小于  lte 小于等于  gt大于  gte大于等于 eq等于 ne不等于
     *                    <p>
     *                    Criteria oCriteria=Criteria.where("createDate").gte(begin).lte(end);
     *                    <p>
     * @param clazz       数据实体类
     * @param sumField    求和字段
     * @param groupFields 分组字段
     * @return 分组求和后的数据集合
     */
    <T> List<T> sum(String collection, Map<String, Object> queryMap, Criteria criteria, Class<T> clazz, String sumField, String... groupFields);

}
