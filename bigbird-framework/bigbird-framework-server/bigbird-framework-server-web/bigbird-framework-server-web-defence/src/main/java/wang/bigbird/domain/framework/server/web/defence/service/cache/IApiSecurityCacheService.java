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
package wang.bigbird.domain.framework.server.web.defence.service.cache;

import wang.bigbird.domain.framework.server.web.defence.domain.pojo.ApiSecurityItem;

/**
 * 接口安全控制配置项缓存服务
 *
 * @author Bigbird
 */
public interface IApiSecurityCacheService {

    /**
     * 根据requestUri获取接口安全配置
     *
     * @param requestApi    请求接口模式串
     * @param requestAction 请求动作
     * @return 接口安全配置信息
     */
    ApiSecurityItem getApiSecurityItem(String requestApi, String requestAction);

}
