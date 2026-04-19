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
package wang.bigbird.domain.framework.server.web.defence.support.security.judge.impl;

import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.server.web.defence.support.security.AccessData;
import wang.bigbird.domain.framework.server.web.defence.domain.pojo.ApiSecurityItem;
import wang.bigbird.domain.framework.server.web.defence.domain.pojo.CallerItem;
import wang.bigbird.domain.framework.server.web.defence.exception.DefenceException;
import wang.bigbird.domain.framework.server.web.defence.support.security.judge.IDefenceJudge;

/**
 * 攻击防御器可用的公共方法
 *
 * @author Bigbird
 */
@Slf4j
public abstract class BaseDefenceJudge implements IDefenceJudge {

    @Override
    public void accept(CallerItem caller, AccessData accessData, ApiSecurityItem serviceSecurityItem) throws DefenceException {
        doAccept(caller, accessData, serviceSecurityItem);
    }

    /**
     * 执行访问安全检查
     *
     * @param caller              调用者
     * @param accessData          调用者携带的请求数据
     * @param serviceSecurityItem 安全策略
     * @throws DefenceException 安全防护访问异常
     */
    protected abstract void doAccept(CallerItem caller, AccessData accessData, ApiSecurityItem serviceSecurityItem) throws DefenceException;

}
