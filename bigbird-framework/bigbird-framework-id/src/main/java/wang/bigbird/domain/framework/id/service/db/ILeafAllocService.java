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
package wang.bigbird.domain.framework.id.service.db;

import wang.bigbird.domain.framework.data.mybatisplus.dynamic.service.base.IService;
import wang.bigbird.domain.framework.id.domain.entity.LeafAlloc;

import java.util.List;

/**
 * @author Bigbird
 */
public interface ILeafAllocService extends IService<LeafAlloc> {

    /**
     * 获取所有业务标识
     *
     * @return
     */
    List<String> loadBizTag();

    /**
     * 按照自定义设置的每次分配的号码数量更新指定业务所被分配的ID号码的最大值
     *
     * @param bizTag   业务标识
     * @param nextStep 自定义号码分配数量
     * @return 更新号码最大值后，返回指定业务的号码分配信息
     */
    LeafAlloc updateMaxIdByCustomStepAndGetLeafAlloc(String bizTag, int nextStep);

    /**
     * 按照数据库设置的每次分配的号码数量更新指定业务所被分配的ID号码的最大值
     *
     * @param bizTag 业务标识
     * @return 更新号码最大值后，返回指定业务的号码分配信息
     */
    LeafAlloc updateMaxIdAndGetLeafAlloc(String bizTag);

}
