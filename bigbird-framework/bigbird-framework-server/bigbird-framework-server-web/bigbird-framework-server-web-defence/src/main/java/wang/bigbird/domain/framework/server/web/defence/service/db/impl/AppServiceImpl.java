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
import wang.bigbird.domain.framework.server.web.defence.base.enums.AppTypeEnum;
import wang.bigbird.domain.framework.server.web.defence.dao.AppMapper;
import wang.bigbird.domain.framework.server.web.defence.domain.bo.AppBO;
import wang.bigbird.domain.framework.server.web.defence.domain.entity.App;
import wang.bigbird.domain.framework.server.web.defence.service.db.IAppService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用信息服务
 *
 * @author Bigbird
 */
@Slf4j
@Service
public class AppServiceImpl extends AbstractServiceImpl<AppMapper, App> implements IAppService {

    @Override
    public void register(AppBO appBO) {
        App app = BeanCopierUtils.copyNotNullProperties(appBO, new App());
        app.setCreateTime(LocalDateTime.now());
        save(app);
    }

    @Override
    public AppBO loadByAppKey(String appKey) {
        QueryWrapper<App> queryWrapper = new QueryWrapper();
        queryWrapper.lambda().eq(App::getAppKey, appKey);
        App app = getOne(queryWrapper);
        return BeanCopierUtils.copyNotNullProperties(app, new AppBO());
    }

    @Override
    public List<Long> loadAppIdByType(AppTypeEnum appType) {
        QueryWrapper<App> queryWrapper = new QueryWrapper();
        queryWrapper.lambda().eq(App::getAppType, appType.idx());
        return list(queryWrapper).stream().map(App::getId).collect(Collectors.toList());
    }

}
