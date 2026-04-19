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

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.server.web.defence.domain.pojo.ApiSecurityItem;
import wang.bigbird.domain.framework.server.web.defence.domain.pojo.CallerItem;
import wang.bigbird.domain.framework.server.web.defence.exception.BadRequestDataException;
import wang.bigbird.domain.framework.server.web.defence.exception.DefenceException;
import wang.bigbird.domain.framework.server.web.defence.exception.IpInvalidException;
import wang.bigbird.domain.framework.server.web.defence.support.security.AccessData;

import java.util.List;

/**
 * IP限制防御器
 *
 * @author Bigbird
 */
@Slf4j
@Component
public class IpLimitDefenceJudge extends BaseDefenceJudge {

    @Override
    public boolean support(ApiSecurityItem serviceSecurityItem) {
        boolean isSupport = serviceSecurityItem != null && serviceSecurityItem.getIpCheckEnable() != null && serviceSecurityItem.getIpCheckEnable();
        return isSupport;
    }

    @Override
    public void doAccept(CallerItem caller, AccessData accessData, ApiSecurityItem serviceSecurityItem) throws DefenceException {
        if (accessData == null || StringUtils.isBlank(accessData.getRemoteAddr())) {
            log.error("AccessData or remoteAddr is null! AccessData: {}", accessData);
            throw new BadRequestDataException("AccessData or remoteAddr is null!");
        }
        if (StringUtils.isBlank(caller.getLimitIps())) {
            String errorInfo = String.format("IP restriction is not configured by the caller[%s]!", caller.getAppKey());
            log.error(errorInfo);
            throw new IpInvalidException(errorInfo);
        }
        List<String> limitIps = Lists.newArrayList(caller.getLimitIps().split(","));
        if (!limitIps.contains(accessData.getRemoteAddr())) {
            String errorInfo = String.format("Request ip [%s] is invalid!", accessData.getRemoteAddr());
            log.error(errorInfo);
            throw new IpInvalidException(errorInfo);
        }
    }

}
