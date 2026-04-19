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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.server.web.defence.domain.pojo.ApiSecurityItem;
import wang.bigbird.domain.framework.server.web.defence.domain.pojo.CallerItem;
import wang.bigbird.domain.framework.server.web.defence.exception.BadRequestDataException;
import wang.bigbird.domain.framework.server.web.defence.exception.DefenceException;
import wang.bigbird.domain.framework.server.web.defence.exception.ReplayAttackException;
import wang.bigbird.domain.framework.server.web.defence.service.cache.INonceCacheService;
import wang.bigbird.domain.framework.server.web.defence.support.security.AccessData;

/**
 * 防重放攻击防御器
 *
 * @author Bigbird
 */
@Slf4j
@Component
public class ReplayAttackDefenceJudge extends BaseDefenceJudge {

    @Autowired
    private INonceCacheService cacheService;

    @Override
    public boolean support(ApiSecurityItem serviceSecurityItem) {
        boolean isSupport = serviceSecurityItem != null && serviceSecurityItem.getAntiReplayEnable() != null && serviceSecurityItem.getAntiReplayEnable();
        return isSupport;
    }

    @Override
    public void doAccept(CallerItem caller, AccessData accessData, ApiSecurityItem serviceSecurityItem) throws DefenceException {
        if (accessData == null || accessData.getRequestParam() == null || StringUtils.isBlank(accessData.getRequestParam().get(AccessData.NONCE_PARAM_CODE))) {
            log.error("AccessData or requestParam or nonce is null! AccessData: {}", accessData);
            throw new BadRequestDataException("AccessData or requestParam or nonce is null!");
        }
        String nonce = accessData.getRequestParam().get(AccessData.NONCE_PARAM_CODE);
        String cacheKey = serviceSecurityItem.getApiCode() + "_" + nonce;
        if (cacheService.get(cacheKey) != null) {
            throw new ReplayAttackException("Replay request.");
        } else {
            cacheService.put(cacheKey);
        }
    }

}
