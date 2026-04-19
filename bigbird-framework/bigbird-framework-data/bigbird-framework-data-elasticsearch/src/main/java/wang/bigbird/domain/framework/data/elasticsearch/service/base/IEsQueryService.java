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

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.data.domain.Sort;
import wang.bigbird.domain.framework.core.base.tool.pageable.PageData;
import wang.bigbird.domain.framework.data.elasticsearch.domain.param.QueryParam;
import wang.bigbird.domain.framework.data.elasticsearch.exception.IllegalQueryParamException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 查询服务
 *
 * @author Bigbird
 */
public interface IEsQueryService {

    /**
     * 查询单条记录
     *
     * @param index 索引名称
     * @param id    主键
     * @param clazz 数据实体类
     * @return 数据实体
     */
    <T> T get(String index, String id, Class<T> clazz);

    /**
     * 查询单条记录
     *
     * @param index  索引名称
     * @param id     主键
     * @param clazz  数据实体类
     * @param fields 需要返回的指定字段
     * @return 数据实体
     */
    <T> T get(String index, String id, Class<T> clazz, List<String> fields);

    /**
     * 通过id列表查询
     *
     * @param index 索引名称
     * @param ids   id列表
     * @param clazz 数据实体类
     * @return 数据实体集合
     */
    <T> List<T> findByIds(String index, Collection<String> ids, Class<T> clazz);

    /**
     * 通过id列表查询
     *
     * @param index  索引名称
     * @param ids    id列表
     * @param clazz  数据实体类
     * @param fields 需要返回的指定字段
     * @return 数据实体集合
     */
    <T> List<T> findByIds(String index, Collection<String> ids, Class<T> clazz, List<String> fields);

    /**
     * 分页查询
     *
     * @param indices      索引名称
     * @param queryBuilder 查询条件
     * @param clazz        数据实体类
     * @param pageNo       当前页
     * @param pageSize     当前页数据条数
     * @return
     */
    <T> PageData<T> findPage(List<String> indices, QueryBuilder queryBuilder, Class<T> clazz, int pageNo, int pageSize);

    /**
     * 分页查询
     *
     * @param indices  索引名称
     * @param queryObj 查询条件
     * @param clazz    数据实体类
     * @param pageNo   当前页
     * @param pageSize 当前页数据条数
     * @return
     */
    <T> PageData<T> findPage(List<String> indices, Object queryObj, Class<T> clazz, int pageNo, int pageSize);

    /**
     * 分页查询
     *
     * @param indices  索引名称
     * @param queryMap 查询条件
     * @param clazz    数据实体类
     * @param pageNo   当前页
     * @param pageSize 当前页数据条数
     * @return
     */
    <T> PageData<T> findPage(List<String> indices, Map<String, Object> queryMap, Class<T> clazz, int pageNo, int pageSize);

    /**
     * 分页查询
     *
     * @param indices   索引名称
     * @param queryObj  查询条件
     * @param clazz     数据实体类
     * @param fields    需要返回的指定字段
     * @param pageNo    当前页
     * @param pageSize  当前页数据条数
     * @param sortField 排序字段
     * @param direction Direction.Desc/ASC 排序方式
     * @return
     */
    <T> PageData<T> findPage(List<String> indices, Object queryObj, Class<T> clazz, List<String> fields, int pageNo, int pageSize, String sortField, Sort.Direction direction);

    /**
     * 分页查询
     *
     * @param indices   索引名称
     * @param queryMap  查询条件
     * @param clazz     数据实体类
     * @param fields    需要返回的指定字段
     * @param pageNo    当前页
     * @param pageSize  当前页数据条数
     * @param sortField 排序字段
     * @param direction Direction.Desc/ASC 排序方式
     * @return
     */
    <T> PageData<T> findPage(List<String> indices, Map<String, Object> queryMap, Class<T> clazz, List<String> fields, int pageNo, int pageSize, String sortField, Sort.Direction direction);

    /**
     * 分页查询
     *
     * @param indices      索引名称
     * @param queryBuilder 查询条件
     * @param clazz        数据实体类
     * @param fields       需要返回的指定字段
     * @param pageNo       当前页
     * @param pageSize     当前页数据条数
     * @param sortField    排序字段
     * @param direction    Direction.Desc/ASC 排序方式
     * @return
     */
    <T> PageData<T> findPage(List<String> indices, QueryBuilder queryBuilder, Class<T> clazz, List<String> fields, int pageNo, int pageSize, String sortField, Sort.Direction direction);

    /**
     * 按条件查询符合条件的数据量
     *
     * @param queryParam 查询对象
     * @return 数据量
     * @throws IllegalQueryParamException
     */
    long count(QueryParam queryParam) throws IllegalQueryParamException;

    /**
     * 按条件查询符合条件的数据，只适合查询10000条范围之内的，
     * 注意，查询数据量有可能超过100M限制，此时在查询条件中限制返回字段是一个比较好的解决方法，
     * 更深入的解决办法，参考：
     * http://note.youdao.com/noteshare?id=64f0270a71d6cafb8ac123af249efeab&sub=BF09EF0A88A14E71B8DC272C46BBB5F7
     *
     * @param queryParam
     * @param clazz
     * @param <T>
     * @return
     * @throws IllegalQueryParamException
     */
    <T> List<T> query(QueryParam queryParam, Class<T> clazz) throws IllegalQueryParamException;

    /**
     * 滚动查询符合条件的数据
     * 注意，查询数据量有可能超过100M限制，
     * 此时在查询条件中限制返回字段是一个比较好的解决方法，
     * 另外一个方法是缩小每次滚动的量度，即设置合适的length值，否则滚动量度默认为10000数据量
     * <p>
     * 更深入的解决办法，参考：
     * http://note.youdao.com/noteshare?id=64f0270a71d6cafb8ac123af249efeab&sub=BF09EF0A88A14E71B8DC272C46BBB5F7
     *
     * @param queryParam
     * @param clazz
     * @param <T>
     * @return
     * @throws IllegalQueryParamException
     */
    <T> List<T> scrollSearch(QueryParam queryParam, Class<T> clazz) throws IllegalQueryParamException;
}
