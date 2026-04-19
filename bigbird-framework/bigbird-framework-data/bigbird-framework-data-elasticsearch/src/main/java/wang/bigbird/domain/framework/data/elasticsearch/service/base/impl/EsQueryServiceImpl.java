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

import cn.hutool.core.bean.BeanUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.CardinalityAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedCardinality;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.tool.pageable.PageData;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.core.exception.ProcessingRuntimeException;
import wang.bigbird.domain.framework.data.elasticsearch.domain.param.QueryParam;
import wang.bigbird.domain.framework.data.elasticsearch.exception.IllegalQueryParamException;
import wang.bigbird.domain.framework.data.elasticsearch.service.base.IEsQueryService;

import java.io.IOException;
import java.util.*;

/**
 * 查询服务
 *
 * @author Bigbird
 */
@Service
public class EsQueryServiceImpl implements IEsQueryService {

    /**
     * 滚动查询时间 60秒
     */
    private static long scrollTimeInMillis = 60000;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public <T> T get(String index,
                     String id,
                     Class<T> clazz) {
        return get(index, id, clazz, null);
    }

    @Override
    public <T> T get(String index,
                     String id,
                     Class<T> clazz,
                     List<String> fields) {
        List<T> queryList = findByIds(index, Collections.singletonList(id), clazz, fields);
        if (CollectionUtils.isEmpty(queryList)) {
            return null;
        }
        return queryList.get(0);
    }

    @Override
    public <T> List<T> findByIds(String index,
                                 Collection<String> ids,
                                 Class<T> clazz) {
        return findByIds(index, ids, clazz, null);
    }

    @Override
    public <T> List<T> findByIds(String index,
                                 Collection<String> ids,
                                 Class<T> clazz,
                                 List<String> fields) {
        TermsQueryBuilder queryBuilder = QueryBuilders.termsQuery("_id", ids);
        PageData<T> page = findPage(Collections.singletonList(index), queryBuilder, clazz, fields, 1, ids.size(), null, null);
        if (0L == page.getTotal()) {
            return Collections.emptyList();
        }
        return page.getList();
    }

    @Override
    public <T> PageData<T> findPage(List<String> indices,
                                    QueryBuilder queryBuilder,
                                    Class<T> clazz,
                                    int pageNo,
                                    int pageSize) {
        return findPage(indices, queryBuilder, clazz, null, pageNo, pageSize, null, null);
    }

    @Override
    public <T> PageData<T> findPage(List<String> indices,
                                    Object queryObj,
                                    Class<T> clazz,
                                    int pageNo,
                                    int pageSize) {
        Map<String, Object> queryMap = toMap(queryObj);
        QueryBuilder queryBuilder = getQueryBuilderWithAnd(queryMap);
        return findPage(indices, queryBuilder, clazz, null, pageNo, pageSize, null, null);
    }

    @Override
    public <T> PageData<T> findPage(List<String> indices,
                                    Map<String, Object> queryMap,
                                    Class<T> clazz,
                                    int pageNo,
                                    int pageSize) {
        QueryBuilder queryBuilder = getQueryBuilderWithAnd(queryMap);
        return findPage(indices, queryBuilder, clazz, null, pageNo, pageSize, null, null);
    }

    @Override
    public <T> PageData<T> findPage(List<String> indices,
                                    Object queryObj,
                                    Class<T> clazz,
                                    List<String> fields,
                                    int pageNo,
                                    int pageSize,
                                    String sortField,
                                    Sort.Direction direction) {
        Map<String, Object> queryMap = toMap(queryObj);
        QueryBuilder queryBuilder = getQueryBuilderWithAnd(queryMap);
        return findPage(indices, queryBuilder, clazz, fields, pageNo, pageSize, sortField, direction);
    }

    @Override
    public <T> PageData<T> findPage(List<String> indices,
                                    Map<String, Object> queryMap,
                                    Class<T> clazz,
                                    List<String> fields,
                                    int pageNo,
                                    int pageSize,
                                    String sortField,
                                    Sort.Direction direction) {
        QueryBuilder queryBuilder = getQueryBuilderWithAnd(queryMap);
        return findPage(indices, queryBuilder, clazz, fields, pageNo, pageSize, sortField, direction);
    }

    @Override
    public <T> PageData<T> findPage(List<String> indices,
                                    QueryBuilder queryBuilder,
                                    Class<T> clazz,
                                    List<String> fields,
                                    int pageNo,
                                    int pageSize,
                                    String sortField,
                                    Sort.Direction direction) {
        if (CollectionUtils.isEmpty(indices)) {
            throw new IllegalArgumentException("indices is empty.");
        }
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        SearchRequest searchRequest = new SearchRequest();
        // index
        searchRequest.indices(indices.toArray(new String[]{}));
        // query
        if (null != queryBuilder) {
            sourceBuilder.query(queryBuilder);
        }
        // fields
        if (CollectionUtils.isNotEmpty(fields)) {
            sourceBuilder.fetchSource(fields.toArray(new String[]{}), null);
        }
        // page
        int from = (pageNo - 1) * pageSize;
        sourceBuilder.from(from);
        sourceBuilder.size(pageSize);
        // sort
        if (StringUtils.isNotBlank(sortField)) {
            SortOrder sortOrder;
            if (Sort.Direction.ASC.equals(direction)) {
                sortOrder = SortOrder.ASC;
            } else {
                sortOrder = SortOrder.DESC;
            }
            sourceBuilder.sort(sortField, sortOrder);
        }
        // request
        searchRequest.source(sourceBuilder);
        SearchResponse response;
        try {
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ProcessingRuntimeException(e);
        }
        SearchHits hits = response.getHits();
        long total = hits.getTotalHits().value;
        if (0L == total) {
            return new PageData<>(pageNo, pageSize, 0L, Collections.emptyList());
        }
        List<T> results = new ArrayList<>();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            T result = JsonUtils.json2Object(json, clazz);
            results.add(result);
        }
        return new PageData<>(pageNo, pageSize, total, results);
    }

    @Override
    public long count(QueryParam queryParam) throws IllegalQueryParamException {
        IndexOperations index = elasticsearchRestTemplate.indexOps(IndexCoordinates.of(queryParam.getIndexName()));
        if (index.exists()) {
            NativeSearchQueryBuilder nativeSearchQueryBuilder = createQueryBuilder(queryParam, true, false);
            NativeSearchQuery searchQuery = nativeSearchQueryBuilder.build();
            if (StringUtils.isNotBlank(queryParam.getCollapseFieldName())) {
                org.springframework.data.elasticsearch.core.SearchHits<Map> search = elasticsearchRestTemplate.search(searchQuery, Map.class, IndexCoordinates.of(queryParam.getIndexName()));
                if (search.hasSearchHits()) {
                    return ((ParsedCardinality) (search.getAggregations().asMap().get(queryParam.getCollapseFieldName()))).getValue();
                }
            } else {
                return elasticsearchRestTemplate.count(searchQuery, IndexCoordinates.of(queryParam.getIndexName()));
            }
        }
        return 0;
    }

    @Override
    public <T> List<T> query(QueryParam queryParam, Class<T> clazz) throws IllegalQueryParamException {
        NativeSearchQueryBuilder builder = createQueryBuilder(queryParam, false, false);
        org.springframework.data.elasticsearch.core.SearchHits<T> search = elasticsearchRestTemplate.search(builder.build(), clazz, IndexCoordinates.of(queryParam.getIndexName()));
        List<T> result = new ArrayList<>();
        if (search.hasSearchHits()) {
            List<org.springframework.data.elasticsearch.core.SearchHit<T>> searchHits = search.getSearchHits();
            for (org.springframework.data.elasticsearch.core.SearchHit<T> searchHit : searchHits) {
                result.add(searchHit.getContent());
            }
        }
        return result;
    }

    @Override
    public <T> List<T> scrollSearch(QueryParam queryParam, Class<T> clazz) throws IllegalQueryParamException {
        NativeSearchQueryBuilder builder = createQueryBuilder(queryParam, false, true);
        List<T> result = new ArrayList<>();
        SearchScrollHits<T> scroll = elasticsearchRestTemplate.searchScrollStart(scrollTimeInMillis, builder.build(), clazz, IndexCoordinates.of(queryParam.getIndexName()));
        List<String> scrollIds = new ArrayList<>();
        // 滚动查询，每一批的起点位置，每一批是10000条数据
        int batchFrom = 0;
        // 分页起始位置，分页或者位于一批次内，或者跨一个批次
        int pageFrom = queryParam.getFrom();
        int pageEnd = queryParam.getFrom() + queryParam.getLength();
        while (scroll.hasSearchHits()) {
            List<org.springframework.data.elasticsearch.core.SearchHit<T>> searchHits = scroll.getSearchHits();
            if (queryParam.isPageable()) {
                // 分页只获取符合分页条件的数据
                int batchSize = searchHits.size();
                int batchEnd = batchFrom + batchSize;
                if (pageFrom >= batchFrom && pageEnd <= batchEnd) {
                    // 表示当前批次完全包含分页数据，截取分页数据段，跳出
                    int start = pageFrom - batchFrom;
                    int end = pageEnd - batchFrom;
                    for (int i = start; i < end; i++) {
                        result.add(searchHits.get(i).getContent());
                    }
                    break;
                } else if (pageFrom >= batchFrom && pageEnd > batchEnd) {
                    // 表示当前批次包含分页数据前部分数据，截取分页数据段
                    int start = pageFrom - batchFrom;
                    int end = batchEnd - batchFrom;
                    for (int i = start; i < end; i++) {
                        result.add(searchHits.get(i).getContent());
                    }
                } else if (pageFrom < batchFrom && pageEnd > batchFrom) {
                    // 表示当前批次包含分页数据后部分数据，截取分页数据段
                    int end = pageEnd - batchFrom;
                    for (int i = 0; i < end; i++) {
                        result.add(searchHits.get(i).getContent());
                    }
                    break;
                }
                batchFrom += batchSize;
            } else {
                // 不分页就获取所有数据
                for (org.springframework.data.elasticsearch.core.SearchHit<T> searchHit : searchHits) {
                    result.add(searchHit.getContent());
                }
            }
            scrollIds.add(scroll.getScrollId());
            //取下一页，scrollId在es服务器上可能会发生变化，需要用最新的。发起continueScroll请求会重新刷新快照保留时间
            scroll = elasticsearchRestTemplate.searchScrollContinue(scroll.getScrollId(), scrollTimeInMillis, clazz, IndexCoordinates.of(queryParam.getIndexName()));
        }
        //及时释放es服务器资源
        elasticsearchRestTemplate.searchScrollClear(scrollIds);
        return result;
    }


    private static BoolQueryBuilder getQueryBuilderWithAnd(Map<String, Object> map) {
        if (MapUtils.isEmpty(map)) {
            return null;
        }
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (null == value) {
                continue;
            }
            if (value instanceof Collection) {
                boolQueryBuilder.must(QueryBuilders.termsQuery(key, (Collection) value));
            } else {
                boolQueryBuilder.must(QueryBuilders.termsQuery(key, value));
            }
        }
        return boolQueryBuilder;
    }

    private static Map<String, Object> toMap(Object o) {
        return (o == null) ? new HashMap<>(2) : BeanUtil.beanToMap(o, false, true);
    }

    /**
     * 构建查询体
     *
     * @param queryParam 查询参数
     * @param statistic  是否统计模式，统计模式下，不需要考虑分页，排序，过滤字段条件
     * @param scroll     是否滚动模式，滚动模式下，不能传递分页参数，滚动模式分页需要依靠查询数据后采用内存分页
     * @return
     */
    private NativeSearchQueryBuilder createQueryBuilder(QueryParam queryParam, boolean statistic, boolean scroll) throws IllegalQueryParamException {
        if (queryParam == null || StringUtils.isBlank(queryParam.getIndexName())) {
            throw new IllegalQueryParamException("Query parameter invalid!");
        }
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        // 设置查询条件
        withQuery(queryParam, builder);
        // 设置范围过滤
        rangeFilter(queryParam, builder);
        if (!statistic) {
            // 设置排序
            sort(queryParam, builder);
            if (scroll) {
                // 滚动查询条件下，不予许设置from，因此必须从0开始查询，但是设置size可以控制每次滚动的量级
                builder.withPageable(PageRequest.of(0, queryParam.getLength()));
            } else {
                // 设置分页
                pageable(queryParam, builder);
            }
            // 设置需要返回的字段集合
            sourceFilter(queryParam, builder);
        }
        // 设置去重
        collapse(queryParam, builder);
        return builder;
    }

    private void collapse(QueryParam queryParam, NativeSearchQueryBuilder builder) {
        if (StringUtils.isNotBlank(queryParam.getCollapseFieldName())) {
            String collapseFieldName = queryParam.getCollapseFieldName();
            builder.withCollapseField(collapseFieldName);
            CardinalityAggregationBuilder aggregationBuilder = AggregationBuilders.cardinality(collapseFieldName).field(collapseFieldName);
            builder.addAggregation(aggregationBuilder);
        }
    }

    private void withQuery(QueryParam queryParam, NativeSearchQueryBuilder builder) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 拼接查询字段集
        withQueryFields(queryParam, queryBuilder);
        // 拼接分组条件
        withGroupFields(queryParam, queryBuilder);
        builder.withQuery(queryBuilder);
    }

    private void withGroupFields(QueryParam queryParam, BoolQueryBuilder queryBuilder) {
        Map<String, List<QueryParam.QueryField>> groupFields = queryParam.getGroupFields();
        if (MapUtils.isNotEmpty(groupFields)) {
            Set<Map.Entry<String, List<QueryParam.QueryField>>> entrys = groupFields.entrySet();
            for (Map.Entry<String, List<QueryParam.QueryField>> entry : entrys) {
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                should(boolQueryBuilder, entry.getValue());
                queryBuilder.must(boolQueryBuilder);
            }
        }
    }

    private void withQueryFields(QueryParam queryParam, BoolQueryBuilder queryBuilder) {
        List<QueryParam.QueryField> queryFields = queryParam.getQueryFields();
        if (CollectionUtils.isNotEmpty(queryFields)) {
            for (QueryParam.QueryField queryField : queryFields) {
                switch (queryField.getLogicalType()) {
                    case OR:
                        queryBuilder.should(loadQueryBuilderByQueryField(queryField));
                        break;
                    case NOT:
                        queryBuilder.mustNot(loadQueryBuilderByQueryField(queryField));
                        break;
                    case AND:
                    default:
                        queryBuilder.must(loadQueryBuilderByQueryField(queryField));
                }
            }
        }
    }

    private void should(BoolQueryBuilder boolQueryBuilder, List<QueryParam.QueryField> queryFields) {
        for (QueryParam.QueryField queryField : queryFields) {
            boolQueryBuilder.should(loadQueryBuilderByQueryField(queryField));
        }
    }

    private void rangeFilter(QueryParam queryParam, NativeSearchQueryBuilder builder) {
        if (queryParam.getRangeFilter() != null) {
            QueryParam.RangeFilter filter = queryParam.getRangeFilter();
            builder.withFilter(QueryBuilders.rangeQuery(filter.getFieldName()).gte(filter.getStart()).lte(filter.getEnd()));
        }
    }

    private void sort(QueryParam queryParam, NativeSearchQueryBuilder builder) {
        if (queryParam.getOrder() != null) {
            builder.withSort(SortBuilders.fieldSort(queryParam.getOrder().getOrderByFieldName())
                    .order(SortOrder.fromString(queryParam.getOrder().getDirection().name())));
        }
    }

    private void pageable(QueryParam queryParam, NativeSearchQueryBuilder builder) {
        int from = queryParam.getFrom();
        int size = queryParam.getLength();
        int page = from / size;
        builder.withPageable(PageRequest.of(page, size));
    }

    private void sourceFilter(QueryParam queryParam, NativeSearchQueryBuilder builder) {
        List<String> sourceInclude = queryParam.getSourceInclude();
        if (CollectionUtils.isNotEmpty(sourceInclude)) {
            SourceFilter sourceFilter = new FetchSourceFilter(sourceInclude.toArray(new String[sourceInclude.size()]), null);
            builder.withSourceFilter(sourceFilter);
        }
    }

    private QueryBuilder loadQueryBuilderByQueryField(QueryParam.QueryField queryField) {
        switch (queryField.getQueryType()) {
            case match:
                // 适合搜索text类型字段，会将搜索词分词，然后判断match的分词结果和text的分词结果是否有相同的，存在交集就匹配
                MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(queryField.getFieldName(), queryField.getFieldValue());
                if (queryField.getAnalyzer() != null) {
                    matchQueryBuilder.analyzer(queryField.getAnalyzer().getName());
                }
                return matchQueryBuilder;
            case match_phrase:
                // 如果用于keyword类型字段，必须完全一致（等同于term），如果用于text类型字段，match_phrase的分词结果必须在text字段分词中都包含，而且顺序必须相同，而且必须都是连续的。
                MatchPhraseQueryBuilder matchPhraseQueryBuilder = QueryBuilders.matchPhraseQuery(queryField.getFieldName(), queryField.getFieldValue());
                if (queryField.getAnalyzer() != null) {
                    matchPhraseQueryBuilder.analyzer(queryField.getAnalyzer().getName());
                }
                return matchPhraseQueryBuilder;
            case match_phrase_prefix:
                // 和 match_phrase 用法是一样的，区别就在于它允许对最后一个词条前缀匹配
                MatchPhrasePrefixQueryBuilder matchPhrasePrefixQueryBuilder = QueryBuilders.matchPhrasePrefixQuery(queryField.getFieldName(), queryField.getFieldValue());
                if (queryField.getAnalyzer() != null) {
                    matchPhrasePrefixQueryBuilder.analyzer(queryField.getAnalyzer().getName());
                }
                return matchPhrasePrefixQueryBuilder;
            case term:
                return QueryBuilders.termQuery(queryField.getFieldName(), queryField.getFieldValue());
            case prefix:
                return QueryBuilders.prefixQuery(queryField.getFieldName(), queryField.getFieldValue());
            default:
                return QueryBuilders.matchAllQuery();
        }
    }

}
