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
package wang.bigbird.domain.framework.id.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import wang.bigbird.domain.framework.data.mybatisplus.dynamic.dao.BaseMapper;
import wang.bigbird.domain.framework.id.domain.entity.LeafAlloc;

/**
 * @author Bigbird
 */
@Repository
public interface LeafAllocMapper extends BaseMapper<LeafAlloc> {

    /**
     * 按照数据库设置的每次分配的号码数量更新指定业务所被分配的ID号码的最大值
     *
     * @param bizTag 业务标识
     */
    void updateMaxId(String bizTag);

    /**
     * 按照自定义的号段数量更新指定业务所被分配的ID号码的最大值
     *
     * @param bizTag 业务标识
     * @param step   自定义的号段数量
     */
    void updateMaxIdByCustomStep(@Param("bizTag") String bizTag, @Param("step") int step);
}
