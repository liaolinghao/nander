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
package wang.bigbird.domain.framework.server.web.defence.support.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.server.web.defence.domain.pojo.ApiSecurityItem;
import wang.bigbird.domain.framework.server.web.defence.domain.pojo.CallerItem;
import wang.bigbird.domain.framework.server.web.defence.exception.DefenceException;
import wang.bigbird.domain.framework.server.web.defence.service.cache.IApiSecurityCacheService;
import wang.bigbird.domain.framework.server.web.defence.support.security.judge.IDefenceJudge;

import java.util.Collection;
import java.util.Map;

/**
 * 安全防护访问网关
 *
 * @author Bigbird
 */
@Slf4j
@Component
public class DefenceGateway implements ApplicationContextAware, InitializingBean {

    private static ApplicationContext applicationContext;

    private static Collection<IDefenceJudge> defenceJudges;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        DefenceGateway.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, IDefenceJudge> judges = applicationContext.getBeansOfType(IDefenceJudge.class);
        defenceJudges = judges.values();
    }

    /**
     * 访问控制
     *
     * @param caller
     * @param accessData
     */
    public static void accessControl(CallerItem caller, AccessData accessData) throws DefenceException {
        if (defenceJudges == null) {
            throw new DefenceException("DefenceJudges has not bean initialized!");
        }
        IApiSecurityCacheService apiSecurityCacheService = applicationContext.getBean(IApiSecurityCacheService.class);
        ApiSecurityItem securityItem = apiSecurityCacheService.getApiSecurityItem(accessData.getRequestApi(), accessData.getRequestAction());
        if (securityItem == null) {
            return;
        }
        //通过安全控制器链的检查
        for (IDefenceJudge judge : defenceJudges) {
            if (judge.support(securityItem)) {
                judge.accept(caller, accessData, securityItem);
            }
        }
    }

}
