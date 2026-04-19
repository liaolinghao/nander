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
package wang.bigbird.domain.framework.data.mybatisplus.dynamic.dao;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Bigbird
 */
public interface BaseMapper<T> extends MPJBaseMapper<T> {
    /**
     * 查询链
     *
     * @return
     */
    default QueryChainWrapper<T> queryChain() {
        return new QueryChainWrapper<>(this);
    }

    /**
     * lambda查询链
     *
     * @return
     */
    default LambdaQueryChainWrapper<T> lambdaQueryChain() {
        return new LambdaQueryChainWrapper<>(this);
    }

    /**
     * 更新链
     *
     * @return
     */
    default UpdateChainWrapper<T> updateChain() {
        return new UpdateChainWrapper<>(this);
    }

    /**
     * lambda更新链
     *
     * @return
     */
    default LambdaUpdateChainWrapper<T> lambdaUpdateChain() {
        return new LambdaUpdateChainWrapper<>(this);
    }

    /**
     * 批量插入
     * 如果实体中属性为null，那么插入数据表中数据为null，
     * 如果数据库中已存在数据，会报错
     *
     * @param entityList
     * @return
     */
    int insertBatchSomeColumn(List<T> entityList);

    /**
     * 批量插入(insert ignore)
     * 如果实体中属性为null，那么插入数据表中数据为null，
     * 如果数据库中已存在数据，会被忽略已存在的数据
     *
     * @param entityList
     * @return
     */
    int insertIgnoreBatchSomeColumn(List<T> entityList);

    /**
     * 根据Id更新固定的某些字段，更新时可以设置哪些字段需要更新，哪些字段不需要更新
     *
     * @param entity
     * @return
     */
    int alwaysUpdateSomeColumnById(@Param(Constants.ENTITY) T entity);

    /**
     * 根据id进行逻辑删除数据，并带自动填充功能
     *
     * @param entity
     * @return
     */
    int deleteByIdWithFill(T entity);
}
