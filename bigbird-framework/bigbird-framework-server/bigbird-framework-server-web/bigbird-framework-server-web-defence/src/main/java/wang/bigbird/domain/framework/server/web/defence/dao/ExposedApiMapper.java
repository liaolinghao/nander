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
package wang.bigbird.domain.framework.server.web.defence.dao;

import org.springframework.stereotype.Repository;
import wang.bigbird.domain.framework.data.mybatisplus.dynamic.dao.BaseMapper;
import wang.bigbird.domain.framework.server.web.defence.domain.entity.ExposedApi;

/**
 * @author Bigbird
 */
@Repository
public interface ExposedApiMapper extends BaseMapper<ExposedApi> {

}
