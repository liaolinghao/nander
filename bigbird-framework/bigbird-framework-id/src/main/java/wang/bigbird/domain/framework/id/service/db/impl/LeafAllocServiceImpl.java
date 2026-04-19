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
package wang.bigbird.domain.framework.id.service.db.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wang.bigbird.domain.framework.data.mybatisplus.dynamic.service.base.impl.AbstractServiceImpl;
import wang.bigbird.domain.framework.id.dao.LeafAllocMapper;
import wang.bigbird.domain.framework.id.domain.entity.LeafAlloc;
import wang.bigbird.domain.framework.id.service.db.ILeafAllocService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bigbird
 */
@Slf4j
@Service
public class LeafAllocServiceImpl extends AbstractServiceImpl<LeafAllocMapper, LeafAlloc> implements ILeafAllocService {

    @Override
    public List<String> loadBizTag() {
        QueryWrapper<LeafAlloc> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("biz_tag");
        return list(queryWrapper).stream().map(LeafAlloc::getBizTag).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public LeafAlloc updateMaxIdByCustomStepAndGetLeafAlloc(String bizTag, int nextStep) {
        getBaseMapper().updateMaxIdByCustomStep(bizTag, nextStep);
        QueryWrapper<LeafAlloc> queryWrapper = new QueryWrapper();
        queryWrapper.lambda().eq(LeafAlloc::getBizTag, bizTag);
        return getOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public LeafAlloc updateMaxIdAndGetLeafAlloc(String bizTag) {
        getBaseMapper().updateMaxId(bizTag);
        QueryWrapper<LeafAlloc> queryWrapper = new QueryWrapper();
        queryWrapper.lambda().eq(LeafAlloc::getBizTag, bizTag);
        return getOne(queryWrapper);
    }
}
