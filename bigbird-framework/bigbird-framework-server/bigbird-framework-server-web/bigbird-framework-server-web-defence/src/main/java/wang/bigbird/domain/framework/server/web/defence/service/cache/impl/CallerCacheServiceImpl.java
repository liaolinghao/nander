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
package wang.bigbird.domain.framework.server.web.defence.service.cache.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import wang.bigbird.domain.framework.core.base.util.BeanCopierUtils;
import wang.bigbird.domain.framework.server.web.defence.domain.bo.AppBO;
import wang.bigbird.domain.framework.server.web.defence.domain.pojo.CallerItem;
import wang.bigbird.domain.framework.server.web.defence.service.cache.ICallerCacheService;
import wang.bigbird.domain.framework.server.web.defence.service.db.IAppService;

/**
 * 调用者缓存服务
 *
 * @author Bigbird
 */
@Service
@DS("defence")
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
public class CallerCacheServiceImpl implements ICallerCacheService {

    private static final String CALLER_CACHE_NAME = "DefenceCallerCache";

    @Autowired
    private IAppService appService;

    @Override
    @Cacheable(value = CALLER_CACHE_NAME, key = "#appKey")
    public CallerItem getCallerByAppKey(String appKey) {
        AppBO appBO = appService.loadByAppKey(appKey);
        return BeanCopierUtils.copyNotNullProperties(appBO, new CallerItem());
    }

}
