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
package wang.bigbird.domain.framework.server.web.defence.support.security.judge;

import wang.bigbird.domain.framework.server.web.defence.support.security.AccessData;
import wang.bigbird.domain.framework.server.web.defence.domain.pojo.ApiSecurityItem;
import wang.bigbird.domain.framework.server.web.defence.domain.pojo.CallerItem;
import wang.bigbird.domain.framework.server.web.defence.exception.DefenceException;

/**
 * 安全防护判断接口
 *
 * @author Bigbird
 */
public interface IDefenceJudge {

    /**
     * 是否支持此接口的检查
     *
     * @param serviceSecurityItem 对外暴露的服务安全控制配置项
     * @return
     */
    boolean support(ApiSecurityItem serviceSecurityItem);

    /**
     * 是否允许访问
     *
     * @param caller              调用者
     * @param accessData          访问数据
     * @param serviceSecurityItem 对外暴露的服务安全控制配置项
     * @throws DefenceException
     */
    void accept(CallerItem caller, AccessData accessData, ApiSecurityItem serviceSecurityItem) throws DefenceException;

}
