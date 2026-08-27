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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.ibatis.annotations.Param;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.data.mybatisplus.dynamic.dao.BaseMapper;
import wang.bigbird.domain.framework.data.mybatisplus.dynamic.service.base.IService;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

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

    /**
     * 分批查询ID集合，通过内存小顶堆聚合算法按分数降序取 TopN
     *
     * @param idSet          待查询ID集合
     * @param batchSize      每批查询数量
     * @param topNum         需要保留前N条
     * @param scoreExtractor 实体获取分数方法引用：Entity::getScore
     * @param batchQueryFunc 单批ID查询回调：传入一批ID集合，返回该批数据
     * @return 按分数降序取 TopN 列表
     */
    public static <T> List<T> batchQueryTopByScore(
            Set<Long> idSet,
            int batchSize,
            int topNum,
            ToDoubleFunction<T> scoreExtractor,
            Function<Set<Long>, List<T>> batchQueryFunc
    ) {
        if (CollectionUtils.isEmpty(idSet)) {
            return Collections.emptyList();
        }
        // Set 转 List 分片
        List<Long> idList = new ArrayList<>(idSet);
        List<List<Long>> batchGroups = Lists.partition(idList, batchSize);
        // 小顶堆：分数升序，堆顶是当前最低分
        PriorityQueue<T> topQueue = new PriorityQueue<>(
                Comparator.comparingDouble(scoreExtractor)
        );
        for (List<Long> batchIds : batchGroups) {
            Set<Long> batchIdSet = Sets.newHashSet(batchIds);
            List<T> batchDataList = batchQueryFunc.apply(batchIdSet);
            if (CollectionUtils.isEmpty(batchDataList)) {
                continue;
            }
            // 逐条入堆，超量剔除最低分
            for (T data : batchDataList) {
                topQueue.add(data);
                if (topQueue.size() > topNum) {
                    topQueue.poll();
                }
            }
        }
        // 反转成高分在前
        LinkedList<T> result = new LinkedList<>();
        while (!topQueue.isEmpty()) {
            result.addFirst(topQueue.poll());
        }
        return result;
    }

    /**
     * 分批查询ID集合后再合并，为配合批量缓存查询方法，要求ID集合为List类型
     *
     * @param idList         待查询ID集合
     * @param batchSize      每批查询数量
     * @param batchQueryFunc 单批ID查询回调：传入一批ID集合，返回该批数据
     * @return 数据集
     */
    public static <T> List<T> batchQuery(
            List<Long> idList,
            int batchSize,
            Function<List<Long>, List<T>> batchQueryFunc
    ) {
        if (CollectionUtils.isEmpty(idList)) {
            return new ArrayList<>();
        }
        List<List<Long>> batchGroups = Lists.partition(idList, batchSize);
        List<T> result = new ArrayList<>(idList.size());
        for (List<Long> batchIds : batchGroups) {
            List<T> batchDataList = batchQueryFunc.apply(batchIds);
            if (CollectionUtils.isEmpty(batchDataList)) {
                continue;
            }
            result.addAll(batchDataList);
        }
        return result;
    }

}
