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

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import wang.bigbird.domain.framework.core.base.tool.pageable.PageData;

import java.util.List;
import java.util.Map;

/**
 * 查询服务
 *
 * @author Bigbird
 */
public interface IMongoDbQueryService {

    /**
     * 通过id查询数据
     *
     * @param collection 集合名称
     * @param id         id
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @return 符合条件的数据实体
     */
    <T> T findById(String collection, Object id, Class<T> clazz);

    /**
     * 查询指定集合中的所有数据
     *
     * @param collection 集合名称
     * @param entity     数据实体类
     * @return 数据实体集合
     */
    <T> List<T> findAll(String collection, Class<T> entity);

    /**
     * 查询指定集合中符合条件的所有数据
     *
     * @param collection 集合名称
     * @param queryObj   查询条件对象
     * @return 符合条件的数据实体集合
     */
    <T> List<T> find(String collection, T queryObj);

    /**
     * 查询指定集合中符合条件的所有数据
     *
     * @param collection 集合名称
     * @param queryObj   查询条件对象，必须是标准 Java Bean（有 getter/setter 的实体类）
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @return 符合条件的数据实体集合
     */
    <T> List<T> find(String collection, Object queryObj, Class<T> clazz);

    /**
     * 查询指定集合中符合条件的所有数据
     *
     * @param collection 集合名称
     * @param queryMap   Map<查询条件key,查询条件value>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @return 符合条件的数据实体集合
     */
    <T> List<T> find(String collection, Map<String, Object> queryMap, Class<T> clazz);

    /**
     * 查询指定集合中符合条件的所有数据
     *
     * @param collection 集合名称
     * @param criteria   示例： lt小于  lte 小于等于  gt大于  gte大于等于 eq等于 ne不等于
     *                   <p>
     *                   Criteria criteria=Criteria.where("createDate").gte(begin).lte(end);
     *                   <p>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @return 符合条件的数据实体集合
     */
    <T> List<T> find(String collection, Criteria criteria, Class<T> clazz);

    /**
     * 查询指定集合中符合条件的所有数据
     *
     * @param collection 集合名称
     * @param queryMap   Map<查询条件key,查询条件value>
     * @param criteria   示例： lt小于  lte 小于等于  gt大于  gte大于等于 eq等于 ne不等于
     *                   <p>
     *                   Criteria criteria=Criteria.where("createDate").gte(begin).lte(end);
     *                   <p>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @return 符合条件的数据实体集合
     */
    <T> List<T> find(String collection, Map<String, Object> queryMap, Criteria criteria, Class<T> clazz);

    /**
     * 查询指定集合中符合条件的所有数据并过滤返回字段
     *
     * @param collection 集合名称
     * @param fields     需要返回的指定字段
     * @param queryObj   查询条件对象
     * @return 符合条件的数据实体集合
     */
    <T> List<T> findFilter(String collection, List<String> fields, T queryObj);

    /**
     * 查询指定集合中的所有数据并过滤返回字段
     *
     * @param collection 集合名称
     * @param fields     需要返回的指定字段
     * @param queryObj   查询条件对象，必须是标准 Java Bean（有 getter/setter 的实体类）
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @return 符合条件的数据实体集合
     */
    <T> List<T> findFilter(String collection, List<String> fields, Object queryObj, Class<T> clazz);

    /**
     * 查询指定集合中的所有数据并过滤返回字段
     *
     * @param collection 集合名称
     * @param fields     需要返回的指定字段
     * @param queryMap   Map<查询条件key,查询条件value>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @return 符合条件的数据实体集合
     */
    <T> List<T> findFilter(String collection, List<String> fields, Map<String, Object> queryMap, Class<T> clazz);

    /**
     * 查询指定集合中的所有数据并过滤返回字段
     *
     * @param collection 集合名称
     * @param fields     需要返回的指定字段
     * @param criteria   示例： lt小于  lte 小于等于  gt大于  gte大于等于 eq等于 ne不等于
     *                   <p>
     *                   Criteria criteria=Criteria.where("createDate").gte(begin).lte(end);
     *                   <p>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @return 符合条件的数据实体集合
     */
    <T> List<T> findFilter(String collection, List<String> fields, Criteria criteria, Class<T> clazz);

    /**
     * 查询指定集合中的所有数据并过滤返回字段
     *
     * @param collection 集合名称
     * @param fields     需要返回的指定字段
     * @param queryMap   Map<查询条件key,查询条件value>
     * @param criteria   示例： lt小于  lte 小于等于  gt大于  gte大于等于 eq等于 ne不等于
     *                   <p>
     *                   Criteria criteria=Criteria.where("createDate").gte(begin).lte(end);
     *                   <p>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @return 符合条件的数据实体集合
     */
    <T> List<T> findFilter(String collection, List<String> fields, Map<String, Object> queryMap, Criteria criteria, Class<T> clazz);

    /**
     * 查询指定集合中符合条件的所有数据并根据字段排序
     *
     * @param collection 集合名称
     * @param queryObj   查询条件对象
     * @param sortField  排序字段
     * @param direction  倒序/正序 Direction.DESC/ASC
     * @return 排序后的符合条件的数据实体集合
     */
    <T> List<T> sortFind(String collection, T queryObj, String sortField, Sort.Direction direction);

    /**
     * 查询指定集合中符合条件的所有数据并根据字段排序
     *
     * @param collection 集合名称
     * @param queryObj   查询条件对象，必须是标准 Java Bean（有 getter/setter 的实体类）
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @param sortField  排序字段
     * @param direction  倒序/正序 Direction.DESC/ASC
     * @return 排序后的符合条件的数据实体集合
     */
    <T> List<T> sortFind(String collection, Object queryObj, Class<T> clazz, String sortField, Sort.Direction direction);

    /**
     * 查询指定集合中符合条件的所有数据并根据字段排序
     *
     * @param collection 集合名称
     * @param queryMap   Map<查询条件key,查询条件value>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @param sortField  排序字段
     * @param direction  倒序/正序 Direction.DESC/ASC
     * @return 排序后的符合条件的数据实体集合
     */
    <T> List<T> sortFind(String collection, Map<String, Object> queryMap, Class<T> clazz, String sortField, Sort.Direction direction);

    /**
     * 查询指定集合中符合条件的所有数据并根据字段排序
     *
     * @param collection 集合名称
     * @param criteria   示例： lt小于  lte 小于等于  gt大于  gte大于等于 eq等于 ne不等于
     *                   <p>
     *                   Criteria criteria=Criteria.where("createDate").gte(begin).lte(end);
     *                   <p>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @param sortField  排序字段
     * @param direction  倒序/正序 Direction.DESC/ASC
     * @return 排序后的符合条件的数据实体集合
     */
    <T> List<T> sortFind(String collection, Criteria criteria, Class<T> clazz, String sortField, Sort.Direction direction);

    /**
     * 查询指定集合中符合条件的所有数据并根据字段排序
     *
     * @param collection 集合名称
     * @param queryMap   Map<查询条件key,查询条件value>
     * @param criteria   示例： lt小于  lte 小于等于  gt大于  gte大于等于 eq等于 ne不等于
     *                   <p>
     *                   Criteria criteria=Criteria.where("createDate").gte(begin).lte(end);
     *                   <p>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @param sortField  排序字段
     * @param direction  倒序/正序 Direction.DESC/ASC
     * @return 排序后的符合条件的数据实体集合
     */
    <T> List<T> sortFind(String collection, Map<String, Object> queryMap, Criteria criteria, Class<T> clazz, String sortField, Sort.Direction direction);

    /**
     * 查询指定集合中符合条件的所有数据并根据字段排序，同时过滤返回字段
     *
     * @param collection 集合名称
     * @param fields     需要返回的指定字段
     * @param queryObj   查询条件对象
     * @param sortField  排序字段
     * @param direction  倒序/正序 Direction.DESC/ASC
     * @return 符合条件的数据实体集合
     */
    <T> List<T> sortFindFilter(String collection, List<String> fields, T queryObj, String sortField, Sort.Direction direction);

    /**
     * 查询指定集合中符合条件的所有数据并根据字段排序，同时过滤返回字段
     *
     * @param collection 集合名称
     * @param fields     需要返回的指定字段
     * @param queryObj   查询条件对象，必须是标准 Java Bean（有 getter/setter 的实体类）
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @param sortField  排序字段
     * @param direction  倒序/正序 Direction.DESC/ASC
     * @return 符合条件的数据实体集合
     */
    <T> List<T> sortFindFilter(String collection, List<String> fields, Object queryObj, Class<T> clazz, String sortField, Sort.Direction direction);

    /**
     * 查询指定集合中符合条件的所有数据并根据字段排序，同时过滤返回字段
     *
     * @param collection 集合名称
     * @param fields     需要返回的指定字段
     * @param queryMap   Map<查询条件key,查询条件value>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @param sortField  排序字段
     * @param direction  倒序/正序 Direction.DESC/ASC
     * @return 符合条件的数据实体集合
     */
    <T> List<T> sortFindFilter(String collection, List<String> fields, Map<String, Object> queryMap, Class<T> clazz, String sortField, Sort.Direction direction);

    /**
     * 查询指定集合中符合条件的所有数据并根据字段排序，同时过滤返回字段
     *
     * @param collection 集合名称
     * @param fields     需要返回的指定字段
     * @param queryMap   Map<查询条件key,查询条件value>
     * @param criteria   示例： lt小于  lte 小于等于  gt大于  gte大于等于 eq等于 ne不等于
     *                   <p>
     *                   Criteria criteria=Criteria.where("createDate").gte(begin).lte(end);
     *                   <p>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @param sortField  排序字段
     * @param direction  倒序/正序 Direction.DESC/ASC
     * @return 符合条件的数据实体集合
     */
    <T> List<T> sortFindFilter(String collection, List<String> fields, Map<String, Object> queryMap, Criteria criteria, Class<T> clazz, String sortField, Sort.Direction direction);

    /**
     * 查询指定集合中符合条件的所有数据并根据字段排序，同时过滤返回字段
     *
     * @param collection 集合名称
     * @param fields     需要返回的指定字段
     * @param queryMap   Map<查询条件key,查询条件value>
     * @param criteria   示例： lt小于  lte 小于等于  gt大于  gte大于等于 eq等于 ne不等于
     *                   <p>
     *                   Criteria criteria=Criteria.where("createDate").gte(begin).lte(end);
     *                   <p>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @param sortField  排序字段
     * @param direction  倒序/正序 Direction.DESC/ASC
     * @param returnId   返回数据是否包含ID
     * @return 符合条件的数据实体集合
     */
    <T> List<T> sortFindFilter(String collection, List<String> fields, Map<String, Object> queryMap, Criteria criteria, Class<T> clazz, String sortField, Sort.Direction direction, Boolean returnId);

    /**
     * 分页查询指定集合中符合条件的数据
     *
     * @param collection 集合名称
     * @param queryObj   查询条件对象
     * @param pageNo     页码，从1开始
     * @param pageSize   每页数据条数
     * @return 符合条件的当页数据实体集合
     */
    <T> PageData<T> pageFind(String collection, T queryObj, int pageNo, int pageSize);

    /**
     * 分页查询指定集合中符合条件的数据
     *
     * @param collection 集合名称
     * @param queryObj   查询条件对象，必须是标准 Java Bean（有 getter/setter 的实体类）
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @param pageNo     页码，从1开始
     * @param pageSize   每页数据条数
     * @return 符合条件的当页数据实体集合
     */
    <T> PageData<T> pageFind(String collection, Object queryObj, Class<T> clazz, int pageNo, int pageSize);

    /**
     * 分页查询指定集合中符合条件的数据
     *
     * @param collection 集合名称
     * @param queryMap   Map<查询条件key,查询条件value>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @param pageNo     页码，从1开始
     * @param pageSize   每页数据条数
     * @return 符合条件的当页数据实体集合
     */
    <T> PageData<T> pageFind(String collection, Map<String, Object> queryMap, Class<T> clazz, int pageNo, int pageSize);

    /**
     * 分页查询指定集合中符合条件的数据
     *
     * @param collection 集合名称
     * @param criteria   示例： lt小于  lte 小于等于  gt大于  gte大于等于 eq等于 ne不等于
     *                   <p>
     *                   Criteria criteria=Criteria.where("createDate").gte(begin).lte(end);
     *                   <p>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @param pageNo     页码，从1开始
     * @param pageSize   每页数据条数
     * @return 符合条件的当页数据实体集合
     */
    <T> PageData<T> pageFind(String collection, Criteria criteria, Class<T> clazz, int pageNo, int pageSize);

    /**
     * 分页查询指定集合中符合条件的数据
     *
     * @param collection 集合名称
     * @param queryMap   Map<查询条件key,查询条件value>
     * @param criteria   示例： lt小于  lte 小于等于  gt大于  gte大于等于 eq等于 ne不等于
     *                   <p>
     *                   Criteria criteria=Criteria.where("createDate").gte(begin).lte(end);
     *                   <p>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @param pageNo     页码，从1开始
     * @param pageSize   每页数据条数
     * @return 符合条件的当页数据实体集合
     */
    <T> PageData<T> pageFind(String collection, Map<String, Object> queryMap, Criteria criteria, Class<T> clazz, int pageNo, int pageSize);

    /**
     * 分页排序查询指定集合中符合条件的数据
     *
     * @param collection 集合名称
     * @param queryObj   查询条件对象
     * @param sortField  排序字段
     * @param direction  倒序/正序 Direction.DESC/ASC
     * @param pageNo     页码，从1开始
     * @param pageSize   每页数据条数
     * @return 符合条件的当页数据实体集合
     */
    <T> PageData<T> pageSortFind(String collection, T queryObj, String sortField, Sort.Direction direction, int pageNo, int pageSize);

    /**
     * 分页排序查询指定集合中符合条件的数据
     *
     * @param collection 集合名称
     * @param queryObj   查询条件对象，必须是标准 Java Bean（有 getter/setter 的实体类）
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @param sortField  排序字段
     * @param direction  倒序/正序 Direction.DESC/ASC
     * @param pageNo     页码，从1开始
     * @param pageSize   每页数据条数
     * @return 符合条件的当页数据实体集合
     */
    <T> PageData<T> pageSortFind(String collection, Object queryObj, Class<T> clazz, String sortField, Sort.Direction direction, int pageNo, int pageSize);

    /**
     * 分页排序查询指定集合中符合条件的数据
     *
     * @param collection 集合名称
     * @param queryMap   Map<查询条件key,查询条件value>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @param sortField  排序字段
     * @param direction  倒序/正序 Direction.DESC/ASC
     * @param pageNo     页码，从1开始
     * @param pageSize   每页数据条数
     * @return 符合条件的当页数据实体集合
     */
    <T> PageData<T> pageSortFind(String collection, Map<String, Object> queryMap, Class<T> clazz, String sortField, Sort.Direction direction, int pageNo, int pageSize);

    /**
     * 分页排序查询指定集合中符合条件的数据
     *
     * @param collection 集合名称
     * @param criteria   示例： lt小于  lte 小于等于  gt大于  gte大于等于 eq等于 ne不等于
     *                   <p>
     *                   Criteria criteria=Criteria.where("createDate").gte(begin).lte(end);
     *                   <p>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @param sortField  排序字段
     * @param direction  倒序/正序 Direction.DESC/ASC
     * @param pageNo     页码，从1开始
     * @param pageSize   每页数据条数
     * @return 符合条件的当页数据实体集合
     */
    <T> PageData<T> pageSortFind(String collection, Criteria criteria, Class<T> clazz, String sortField, Sort.Direction direction, int pageNo, int pageSize);

    /**
     * 分页排序查询指定集合中符合条件的数据
     *
     * @param collection 集合名称
     * @param queryMap   Map<查询条件key,查询条件value>
     * @param criteria   示例： lt小于  lte 小于等于  gt大于  gte大于等于 eq等于 ne不等于
     *                   <p>
     *                   Criteria criteria=Criteria.where("createDate").gte(begin).lte(end);
     *                   <p>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @param sortField  排序字段
     * @param direction  倒序/正序 Direction.DESC/ASC
     * @param pageNo     页码，从1开始
     * @param pageSize   每页数据条数
     * @return 符合条件的当页数据实体集合
     */
    <T> PageData<T> pageSortFind(String collection, Map<String, Object> queryMap, Criteria criteria, Class<T> clazz, String sortField, Sort.Direction direction, int pageNo, int pageSize);

    /**
     * 分页排序查询指定集合中符合条件的数据并过滤返回字段
     *
     * @param collection 集合名称
     * @param fields     需要返回的指定字段
     * @param queryObj   查询条件对象
     * @param sortField  排序字段
     * @param direction  Direction.Desc/ASC 排序方式
     * @param pageNo     页码，从1开始
     * @param pageSize   每页数据条数
     * @return 符合条件的当页数据实体集合
     */
    <T> PageData<T> pageSortFindFilter(String collection, List<String> fields, T queryObj, String sortField, Sort.Direction direction, int pageNo, int pageSize);

    /**
     * 分页排序查询指定集合中符合条件的数据并过滤返回字段
     *
     * @param collection 集合名称
     * @param fields     需要返回的指定字段
     * @param queryObj   查询条件对象，必须是标准 Java Bean（有 getter/setter 的实体类）
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @param sortField  排序字段
     * @param direction  Direction.Desc/ASC 排序方式
     * @param pageNo     页码，从1开始
     * @param pageSize   每页数据条数
     * @return 符合条件的当页数据实体集合
     */
    <T> PageData<T> pageSortFindFilter(String collection, List<String> fields, Object queryObj, Class<T> clazz, String sortField, Sort.Direction direction, int pageNo, int pageSize);

    /**
     * 分页排序查询指定集合中符合条件的数据并过滤返回字段
     *
     * @param collection 集合名称
     * @param fields     需要返回的指定字段
     * @param queryMap   Map<查询条件key,查询条件value>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @param sortField  排序字段
     * @param direction  Direction.Desc/ASC 排序方式
     * @param pageNo     页码，从1开始
     * @param pageSize   每页数据条数
     * @return 符合条件的当页数据实体集合
     */
    <T> PageData<T> pageSortFindFilter(String collection, List<String> fields, Map<String, Object> queryMap, Class<T> clazz, String sortField, Sort.Direction direction, int pageNo, int pageSize);

    /**
     * 分页排序查询指定集合中符合条件的数据并过滤返回字段
     *
     * @param collection 集合名称
     * @param fields     需要返回的指定字段
     * @param criteria   示例： lt小于  lte 小于等于  gt大于  gte大于等于 eq等于 ne不等于
     *                   <p>
     *                   Criteria criteria=Criteria.where("createDate").gte(begin).lte(end);
     *                   <p>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @param sortField  排序字段
     * @param direction  倒序/正序 Direction.DESC/ASC
     * @param pageNo     页码，从1开始
     * @param pageSize   每页数据条数
     * @return 符合条件的当页数据实体集合
     */
    <T> PageData<T> pageSortFindFilter(String collection, List<String> fields, Criteria criteria, Class<T> clazz, String sortField, Sort.Direction direction, int pageNo, int pageSize);

    /**
     * 分页排序查询指定集合中符合条件的数据并过滤返回字段
     *
     * @param collection 集合名称
     * @param fields     需要返回的指定字段
     * @param queryMap   Map<查询条件key,查询条件value>
     * @param criteria   示例： lt小于  lte 小于等于  gt大于  gte大于等于 eq等于 ne不等于
     *                   <p>
     *                   Criteria criteria=Criteria.where("createDate").gte(begin).lte(end);
     *                   <p>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @param sortField  排序字段
     * @param direction  倒序/正序 Direction.DESC/ASC
     * @param pageNo     页码，从1开始
     * @param pageSize   每页数据条数
     * @return 符合条件的当页数据实体集合
     */
    <T> PageData<T> pageSortFindFilter(String collection, List<String> fields, Map<String, Object> queryMap, Criteria criteria, Class<T> clazz, String sortField, Sort.Direction direction, int pageNo, int pageSize);

    /**
     * 查询指定集合中符合条件的数据数量
     *
     * @param collection 集合名称
     * @param queryObj   查询条件对象，必须是标准 Java Bean（有 getter/setter 的实体类）
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @return 符合条件的数据数量
     */
    long count(String collection, Object queryObj, Class<?> clazz);

    /**
     * 查询指定集合中符合条件的数据数量
     *
     * @param collection 集合名称
     * @param queryMap   Map<查询条件key,查询条件value>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @return 符合条件的数据数量
     */
    long count(String collection, Map<String, Object> queryMap, Class<?> clazz);

    /**
     * 查询指定集合中符合条件的数据数量
     *
     * @param collection 集合名称
     * @param criteria   示例： lt小于  lte 小于等于  gt大于  gte大于等于 eq等于 ne不等于
     *                   <p>
     *                   Criteria criteria=Criteria.where("createDate").gte(begin).lte(end);
     *                   <p>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @return 符合条件的数据数量
     */
    long count(String collection, Criteria criteria, Class<?> clazz);

    /**
     * 查询指定集合中符合条件的数据数量
     *
     * @param collection 集合名称
     * @param queryMap   Map<查询条件key,查询条件value>
     * @param criteria   示例： lt小于  lte 小于等于  gt大于  gte大于等于 eq等于 ne不等于
     *                   <p>
     *                   Criteria criteria=Criteria.where("createDate").gte(begin).lte(end);
     *                   <p>
     * @param clazz      数据实体类，根据 clazz 做实体类映射处理，通过 @Field 注解自动映射
     * @return 符合条件的数据数量
     */
    long count(String collection, Map<String, Object> queryMap, Criteria criteria, Class<?> clazz);

}
