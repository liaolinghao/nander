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
package wang.bigbird.domain.framework.id.support;

import wang.bigbird.domain.framework.id.base.enums.IdStrategyEnum;

/**
 * ID生成器
 *
 * @author Bigbird
 */
public interface IdGenerator {

    /**
     * 获取ID生成策略
     *
     * @return ID生成策略
     */
    IdStrategyEnum getStrategy();

    /**
     * 获取ID
     *
     * @param bizTag 业务标识
     * @return id值
     */
    long getUid(String bizTag);

    /**
     * 解析ID
     *
     * @param uid    id值
     * @param bizTag 业务标识
     * @return uid组成部分描述信息
     */
    String parseUid(long uid, String bizTag);

}
