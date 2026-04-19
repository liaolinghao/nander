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
package wang.bigbird.domain.framework.server.web.defence.service.db;

import wang.bigbird.domain.framework.data.mybatisplus.dynamic.service.base.IService;
import wang.bigbird.domain.framework.server.web.defence.base.enums.AppTypeEnum;
import wang.bigbird.domain.framework.server.web.defence.domain.bo.AppBO;
import wang.bigbird.domain.framework.server.web.defence.domain.entity.App;

import java.util.List;


/**
 * 应用信息服务
 *
 * @author Bigbird
 */
public interface IAppService extends IService<App> {

    /**
     * 登记应用
     *
     * @param appBO 应用信息
     * @return 应用信息
     */
    void register(AppBO appBO);

    /**
     * 根据appKey获取应用
     *
     * @param appKey
     * @return 应用信息
     */
    AppBO loadByAppKey(String appKey);

    /**
     * 获取指定类型的应用ID信息
     *
     * @param appType
     * @return 应用ID信息集合
     */
    List<Long> loadAppIdByType(AppTypeEnum appType);
}
