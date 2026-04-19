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
package wang.bigbird.domain.framework.data.elasticsearch.domain.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;
import wang.bigbird.domain.framework.data.elasticsearch.base.constant.QueryConstant;
import wang.bigbird.domain.framework.data.elasticsearch.base.enums.AnalyzerEnum;
import wang.bigbird.domain.framework.data.elasticsearch.base.enums.LogicalTypeEnum;
import wang.bigbird.domain.framework.data.elasticsearch.base.enums.QueryTypeEnum;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 自带查询范围校正的ES搜索参数封装
 *
 * @author Bigbird
 */
public class QueryParam implements Serializable {

    private static final long serialVersionUID = -5607896585645470578L;

    /**
     * 是否分页查询，默认不分页
     */
    private boolean pageable;

    /**
     * 记录开始位置，默认从0开始
     */
    private int from;

    /**
     * 每次查询返回结果集大小，默认为ES最大结果集窗口值
     */
    private int length = QueryConstant.MAX_RESULT_SIZE;

    /**
     * 索引名称
     */
    private String indexName;

    /**
     * 以OR为关系的一组查询条件，该组查询条件被当做一个查询字段看待，对应用于如下查询场景中：
     * <p>
     * A=xx and (B=xx or C=xx) and (D=xx or E=xx)
     * <p>
     * 此时，可以将B、C作为一组，并赋予一个标识
     * <p>
     * 分组查询条件不是必要条件
     */
    private Map<String, List<QueryField>> groupFields;

    /**
     * 查询字段，必要条件
     */
    private List<QueryField> queryFields;

    /**
     * 过滤条件，指定字段值以及值范围
     */
    private RangeFilter rangeFilter;

    /**
     * 排序
     */
    private Order order;

    /**
     * 设置需要返回的字段
     */
    private List<String> sourceInclude;

    /**
     * 去重字段名称
     */
    private String collapseFieldName;

    private QueryParam(Builder builder) {
        setPageable(builder.pageable);
        setFrom(builder.from);
        setLength(builder.length);
        setIndexName(builder.indexName);
        setGroupFields(builder.groupFields);
        setQueryFields(builder.queryFields);
        setRangeFilter(builder.rangeFilter);
        setOrder(builder.order);
        setSourceInclude(builder.sourceInclude);
        setCollapseFieldName(builder.collapseFieldName);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryField implements Serializable {

        private static final long serialVersionUID = -2908094799437196948L;

        /**
         * 字段名称
         */
        private String fieldName;

        /**
         * 字段值
         */
        private String fieldValue;

        /**
         * 查询匹配类型，模糊或者精确
         */
        private QueryTypeEnum queryType = QueryTypeEnum.match;

        /**
         * 多字段查询时，该字段应该被放入的组合条件集合
         */
        private LogicalTypeEnum logicalType = LogicalTypeEnum.AND;

        /**
         * 分词方式
         */
        private AnalyzerEnum analyzer;

    }

    /**
     * 指定只返回某个字段值在某个范围内的结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RangeFilter<T> implements Serializable {

        private static final long serialVersionUID = -5664565623621529945L;

        private String fieldName;

        private T start;

        private T end;

    }

    /**
     * 指定排序方式与排序字段，升序或者降序
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Order implements Serializable {

        private static final long serialVersionUID = -7347045231496872656L;

        private Sort.Direction direction;

        private String orderByFieldName;

    }

    public boolean isPageable() {
        return pageable;
    }

    public void setPageable(boolean pageable) {
        this.pageable = pageable;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        if (from > 0) {
            this.from = from;
        }
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        if (length <= QueryConstant.MAX_RESULT_SIZE) {
            this.length = length;
        }
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public Map<String, List<QueryField>> getGroupFields() {
        return groupFields;
    }

    public void setGroupFields(Map<String, List<QueryField>> groupFields) {
        this.groupFields = groupFields;
    }

    public List<QueryField> getQueryFields() {
        return queryFields;
    }

    public void setQueryFields(List<QueryField> queryFields) {
        this.queryFields = queryFields;
    }

    public RangeFilter getRangeFilter() {
        return rangeFilter;
    }

    public void setRangeFilter(RangeFilter rangeFilter) {
        this.rangeFilter = rangeFilter;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<String> getSourceInclude() {
        return sourceInclude;
    }

    public void setSourceInclude(List<String> sourceInclude) {
        this.sourceInclude = sourceInclude;
    }

    public String getCollapseFieldName() {
        return collapseFieldName;
    }

    public void setCollapseFieldName(String collapseFieldName) {
        this.collapseFieldName = collapseFieldName;
    }

    public static final class Builder {
        private boolean pageable;
        private int from;
        private int length;
        private String indexName;
        private Map<String, List<QueryField>> groupFields;
        private List<QueryField> queryFields;
        private RangeFilter rangeFilter;
        private Order order;
        private List<String> sourceInclude;
        private String collapseFieldName;

        public Builder() {
        }

        public Builder pageable(boolean val) {
            pageable = val;
            return this;
        }

        public Builder from(int val) {
            from = val;
            return this;
        }

        public Builder length(int val) {
            length = val;
            return this;
        }

        public Builder indexName(String val) {
            indexName = val;
            return this;
        }

        public Builder groupFields(Map<String, List<QueryField>> val) {
            groupFields = val;
            return this;
        }

        public Builder queryFields(List<QueryField> val) {
            queryFields = val;
            return this;
        }

        public Builder rangeFilter(RangeFilter val) {
            rangeFilter = val;
            return this;
        }

        public Builder order(Order val) {
            order = val;
            return this;
        }

        public Builder sourceInclude(List<String> val) {
            sourceInclude = val;
            return this;
        }

        public Builder collapseFieldName(String val) {
            collapseFieldName = val;
            return this;
        }

        public QueryParam build() {
            return new QueryParam(this);
        }
    }
}
