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
package wang.bigbird.domain.framework.server.web.defence.service.db.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.util.BeanCopierUtils;
import wang.bigbird.domain.framework.data.mybatisplus.dynamic.service.base.impl.AbstractServiceImpl;
import wang.bigbird.domain.framework.server.web.defence.dao.ExposedApiMapper;
import wang.bigbird.domain.framework.server.web.defence.domain.bo.ExposedApiBO;
import wang.bigbird.domain.framework.server.web.defence.domain.entity.ExposedApi;
import wang.bigbird.domain.framework.server.web.defence.service.db.IExposedApiService;

/**
 * 对外暴露服务API信息服务
 *
 * @author Bigbird
 */
@Slf4j
@Service
public class ExposedApiServiceImpl extends AbstractServiceImpl<ExposedApiMapper, ExposedApi> implements IExposedApiService {

    @Override
    public ExposedApiBO loadByApiUrlAndType(String apiUrl, String apiType) {
        QueryWrapper<ExposedApi> queryWrapper = new QueryWrapper();
        queryWrapper.lambda().eq(ExposedApi::getApiUrl, apiUrl).eq(ExposedApi::getApiType, apiType);
        ExposedApi exposedApi = getOne(queryWrapper);
        return BeanCopierUtils.copyNotNullProperties(exposedApi, new ExposedApiBO());
    }

}
