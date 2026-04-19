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

import wang.bigbird.domain.framework.server.web.defence.support.security.AccessData;
import wang.bigbird.domain.framework.server.web.defence.domain.pojo.ApiSecurityItem;
import wang.bigbird.domain.framework.server.web.defence.domain.pojo.CallerItem;
import wang.bigbird.domain.framework.server.web.defence.exception.BadRequestDataException;
import wang.bigbird.domain.framework.server.web.defence.exception.DefenceException;
import wang.bigbird.domain.framework.server.web.defence.exception.LimitListValidateException;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 黑白名单限制防御器
 *
 * @author Bigbird
 */
@Slf4j
@Component
public class WhiteBlackLimitListDefenceJudge extends BaseDefenceJudge {

    @Override
    public boolean support(ApiSecurityItem serviceSecurityItem) {
        boolean isSupport = serviceSecurityItem != null && (StringUtils.isNotBlank(serviceSecurityItem.getIpWhiteList()) || StringUtils.isNotBlank(serviceSecurityItem.getIpBlackList()) || StringUtils.isNotBlank(serviceSecurityItem.getAppWhiteList()) || StringUtils.isNotBlank(serviceSecurityItem.getAppBlackList()));
        return isSupport;
    }

    @Override
    protected void doAccept(CallerItem caller, AccessData accessData, ApiSecurityItem serviceSecurityItem) throws DefenceException {
        if (accessData == null) {
            throw new BadRequestDataException("AccessData is null!");
        }
        appWhiteListValidate(caller, accessData, serviceSecurityItem);
        appBlackListValidate(caller, accessData, serviceSecurityItem);
        ipWhiteListValidate(accessData, serviceSecurityItem);
        ipBlackListValidate(accessData, serviceSecurityItem);
    }

    private void ipBlackListValidate(AccessData accessData, ApiSecurityItem serviceSecurityItem) {
        if (StringUtils.isBlank(accessData.getRemoteAddr())) {
            throw new BadRequestDataException("Request remoteAddr is null!");
        }
        if (StringUtils.isBlank(serviceSecurityItem.getIpBlackList())) {
            return;
        }
        List<String> ipBlackLists = Lists.newArrayList(serviceSecurityItem.getIpBlackList().split(","));
        if (ipBlackLists.contains(accessData.getRemoteAddr())) {
            //在黑名单列表中
            String errorInfo = String.format("Request remoteAddr[%s] is in black list!", accessData.getRemoteAddr());
            log.error(errorInfo);
            throw new LimitListValidateException(errorInfo);
        }
    }

    private void ipWhiteListValidate(AccessData accessData, ApiSecurityItem serviceSecurityItem) {
        if (StringUtils.isBlank(accessData.getRemoteAddr())) {
            throw new BadRequestDataException("Request remoteAddr is null!");
        }
        if (StringUtils.isBlank(serviceSecurityItem.getIpWhiteList())) {
            return;
        }
        List<String> ipWhiteLists = Lists.newArrayList(serviceSecurityItem.getIpWhiteList().split(","));
        if (ipWhiteLists.contains(accessData.getRemoteAddr())) {
            //在白名单列表中
            return;
        }
        String errorInfo = String.format("Request remoteAddr[%s] is not in white list!", accessData.getRemoteAddr());
        log.error(errorInfo);
        throw new LimitListValidateException(errorInfo);
    }

    private void appBlackListValidate(CallerItem caller, AccessData accessData, ApiSecurityItem serviceSecurityItem) {
        if (StringUtils.isBlank(serviceSecurityItem.getAppBlackList())) {
            return;
        }
        List<String> appBlackLists = Lists.newArrayList(serviceSecurityItem.getAppBlackList().split(","));
        if (appBlackLists.contains(caller.getAppKey())) {
            //在黑名单列表中
            String errorInfo = String.format("AppKey[%s] is in black list!", caller.getAppKey());
            log.error(errorInfo);
            throw new LimitListValidateException(errorInfo);
        }
    }

    private void appWhiteListValidate(CallerItem caller, AccessData accessData, ApiSecurityItem serviceSecurityItem) {
        if (StringUtils.isBlank(serviceSecurityItem.getAppWhiteList())) {
            return;
        }
        List<String> appWhiteLists = Lists.newArrayList(serviceSecurityItem.getAppWhiteList().split(","));
        if (appWhiteLists.contains(caller.getAppKey())) {
            //在白名单列表中
            return;
        }
        String errorInfo = String.format("AppKey[%s] is not in white list!", caller.getAppKey());
        log.error(errorInfo);
        throw new LimitListValidateException(errorInfo);
    }
}
