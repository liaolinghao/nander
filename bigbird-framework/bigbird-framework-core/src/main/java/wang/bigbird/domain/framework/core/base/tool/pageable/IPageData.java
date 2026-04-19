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
package wang.bigbird.domain.framework.core.base.tool.pageable;

import java.util.List;

/**
 * 分页数据接口
 *
 * @author Bigbird
 */
public interface IPageData<T> {

    /**
     * 当前页码
     *
     * @return 页码
     */
    int getPage();

    /**
     * 每页显示总记录数
     *
     * @return 每页显示总记录数
     */
    int getPageSize();

    /**
     * 总记录数
     *
     * @return 总记录数
     */
    long getTotal();

    /**
     * 列表数据
     *
     * @return 列表数据
     */
    List<T> getList();
}
