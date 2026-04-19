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
package wang.bigbird.domain.framework.data.mybatisplus.dynamic.service.base.impl;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.apache.ibatis.annotations.Param;
import wang.bigbird.domain.framework.data.mybatisplus.dynamic.dao.BaseMapper;
import wang.bigbird.domain.framework.data.mybatisplus.dynamic.service.base.IService;

import java.util.List;

/**
 * 扩展ServiceImpl
 *
 * @author Bigbird
 */
public abstract class AbstractServiceImpl<M extends BaseMapper<T>, T> extends MPJBaseServiceImpl<M, T> implements IService<T> {

    @Override
    public int insertBatchSomeColumn(List<T> entityList) {
        return getBaseMapper().insertBatchSomeColumn(entityList);
    }

    @Override
    public int insertIgnoreBatchSomeColumn(List<T> entityList) {
        return getBaseMapper().insertIgnoreBatchSomeColumn(entityList);
    }

    @Override
    public int alwaysUpdateSomeColumnById(@Param(Constants.ENTITY) T entity) {
        return getBaseMapper().alwaysUpdateSomeColumnById(entity);
    }

    @Override
    public int deleteByIdWithFill(T entity) {
        return getBaseMapper().deleteByIdWithFill(entity);
    }

}
