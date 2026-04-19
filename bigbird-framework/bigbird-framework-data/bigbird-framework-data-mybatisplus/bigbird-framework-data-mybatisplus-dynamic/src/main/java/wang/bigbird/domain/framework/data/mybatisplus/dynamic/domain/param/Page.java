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
package wang.bigbird.domain.framework.data.mybatisplus.dynamic.domain.param;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import lombok.Data;
import wang.bigbird.domain.framework.core.base.util.BeanCopierUtils;
import wang.bigbird.domain.framework.core.base.tool.pageable.IPageData;
import wang.bigbird.domain.framework.core.base.tool.pageable.param.IPageable;
import wang.bigbird.domain.framework.core.base.tool.pageable.param.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 新版分页规范
 * 适配mybatisPlus分页插件
 *
 * @author Bigbird
 */
@Data
public class Page<D> implements IPage<D>, IPageData<D> {

    private static final long serialVersionUID = 551178096580953119L;

    private int page = 1;

    private int pageSize = 10;

    private long total;

    private List<D> list = new ArrayList<>();

    private Boolean searchCount = true;

    /**
     * 排序字段信息
     */
    protected List<OrderItem> orders = new ArrayList<>();

    @Override
    public List<OrderItem> orders() {
        return this.getOrders();
    }

    @Override
    public List<D> getRecords() {
        return this.list;
    }

    @Override
    public Page<D> setRecords(List<D> records) {
        this.list = records;
        return this;
    }

    @Override
    public long getTotal() {
        return this.total;
    }

    @Override
    public Page<D> setTotal(long total) {
        this.total = total;
        return this;
    }

    @Override
    public long getSize() {
        return pageSize;
    }

    @Override
    public boolean isSearchCount() {
        return searchCount;
    }

    public Page<D> setSearchCount(boolean searchCount) {
        this.searchCount = searchCount;
        return this;
    }

    @Override
    public long getPages() {
        if (getPageSize() == 0) {
            return 0L;
        }
        long pages = getTotal() / getPageSize();
        if (getTotal() % getPageSize() != 0) {
            pages++;
        }
        return pages;
    }

    @Override
    public Page<D> setSize(long size) {
        this.pageSize = (int) size;
        return this;
    }

    @Override
    public long getCurrent() {
        return this.page;
    }

    @Override
    public Page<D> setCurrent(long current) {
        this.page = Math.toIntExact(current);
        return this;
    }

    /**
     * @param pageable {@link IPageable}
     * @param <T>      数据类型
     * @return T
     * @since 1.2.1 适配分页新规范
     */
    public static <T> Page<T> of(IPageable pageable) {
        Page<T> page = new Page<>();
        page.setCurrent(Optional.ofNullable(pageable.getPage()).orElse(1));
        page.setPageSize(Optional.ofNullable(pageable.getPageSize()).orElse(10));
        page.setSearchCount(Optional.ofNullable(pageable.isSearchCount()).orElse(true));
        List<Order> orders = pageable.getSort();
        if (orders != null) {
            List<OrderItem> orderItems = orders.stream().map(order -> BeanCopierUtils.copyNotNullProperties(order, new OrderItem())).collect(Collectors.toList());
            page.setOrders(orderItems);
        }
        return page;
    }
}
