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
package wang.bigbird.domain.framework.server.web.core.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wang.bigbird.domain.framework.core.base.tool.pageable.IPageData;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.server.web.core.domain.param.PageParam;
import wang.bigbird.domain.framework.server.web.core.support.serializer.CustomLongSerializer;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页数据响应报文
 *
 * @author Bigbird
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@ApiModel
public class PageVO<D> implements IPageData<D>, Serializable {

    private static final long serialVersionUID = 2708885306097752915L;

    @ApiModelProperty("页码")
    private int page;

    @ApiModelProperty("每页显示的总记录数")
    private int pageSize;

    @ApiModelProperty("当前页返回数量")
    private int size;

    @ApiModelProperty("总记录数")
    @JsonSerialize(using = CustomLongSerializer.class)
    private long total;

    @ApiModelProperty("记录")
    private List<D> list;

    public PageVO<D> setPage(int page) {
        this.page = page;
        return this;
    }

    public PageVO<D> setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public PageVO<D> setSize(int size) {
        this.size = size;
        return this;
    }

    public PageVO<D> setTotal(long total) {
        this.total = total;
        return this;
    }

    public PageVO<D> setList(List<D> list) {
        this.list = list;
        return this;
    }

    /**
     * 分页数据响应报文
     *
     * @param pageData {@link IPageData}
     * @param <T>      数据类型
     * @return 分页数据响应报文
     */
    public static <T> PageVO<T> of(IPageData<T> pageData) {
        int page = Optional.ofNullable(pageData).map(IPageData::getPage).orElse(1);
        int pageSize = Optional.ofNullable(pageData).map(IPageData::getPageSize).orElse(0);
        int size = Optional.ofNullable(pageData).map(IPageData::getList).map(List::size).orElse(0);
        long total = Optional.ofNullable(pageData).map(IPageData::getTotal).orElse(0L);
        List<T> list = Optional.ofNullable(pageData).map(IPageData::getList).orElse(Collections.emptyList());
        return new PageVO<>(page, pageSize, size, total, list);
    }

    /**
     * 默认结果为空的分页数据响应报文
     *
     * @param <T> 数据类型
     * @return 结果为空的分页数据响应报文
     */
    public static <T> PageVO<T> empty() {
        return new PageVO<T>(1, 0, 0, 0L, Collections.emptyList());
    }

    /**
     * 对应分页参数结果为空的分页数据响应报文
     *
     * @param pageableParam 分页参数
     * @return 对应分页参数结果为空的分页数据响应报文
     */
    public static <T> PageVO<T> empty(PageParam pageableParam) {
        return new PageVO<T>(pageableParam.getPage(), pageableParam.getPageSize(), 0, 0L, Collections.emptyList());
    }

    /**
     * 当前页数据类型转换
     * 用于结果返回前端前想要对当前页对象进行转换的情况
     * 一般可用于将数据库取出来的分页数据Entity转换为DTO或者VO对象，
     * 可减少网络IO数据传输量
     *
     * @return 新的数据类型分页对象
     */
    public <M> PageVO<M> transfer(Function<D, M> function) {
        List<D> oldList = this.list;
        if (CollectionUtils.isEmpty(oldList)) {
            return new PageVO<>(this.page, this.pageSize, this.size, this.total, Collections.emptyList());
        }
        List<M> newList = this.list.stream().map(function).collect(Collectors.toList());
        return new PageVO<>(this.page, this.pageSize, this.size, this.total, newList);
    }
}
