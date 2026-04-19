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
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.server.web.core.base.util.SignatureUtils;
import wang.bigbird.domain.framework.server.web.defence.domain.pojo.ApiSecurityItem;
import wang.bigbird.domain.framework.server.web.defence.domain.pojo.CallerItem;
import wang.bigbird.domain.framework.server.web.defence.exception.BadRequestDataException;
import wang.bigbird.domain.framework.server.web.defence.exception.DefenceException;
import wang.bigbird.domain.framework.server.web.defence.exception.SignatureInvalidException;
import wang.bigbird.domain.framework.server.web.defence.support.security.AccessData;

/**
 * 签名校验防御器
 *
 * @author Bigbird
 */
@Slf4j
@Component
public class SignatureDefenceJudge extends BaseDefenceJudge {

    @Override
    public boolean support(ApiSecurityItem serviceSecurityItem) {
        boolean isSupport = serviceSecurityItem != null && serviceSecurityItem.getSignEnable() != null && serviceSecurityItem.getSignEnable();
        return isSupport;
    }

    @Override
    protected void doAccept(CallerItem caller, AccessData accessData, ApiSecurityItem serviceSecurityItem) throws DefenceException {
        if (accessData == null || StringUtils.isBlank(accessData.getRequestUri())) {
            log.error("AccessData or requestUri is null! AccessData: {}", accessData);
            throw new BadRequestDataException("AccessData or requestUri is null!");
        }
        String signature = getSignature(accessData);
        if (StringUtils.isBlank(signature)) {
            throw new SignatureInvalidException("Signature is null!");
        }
        String localSignature = SignatureUtils.getMd5SignData(accessData.getRequestUri(), accessData.getRequestParam(), accessData.getRequestHeader(), accessData.getRequestBody(), caller.getAppSecret());
        if (!localSignature.equals(signature)) {
            String errorInfo = String.format("Signature is invalid! Local signature is [%s], Incoming signature is [%s], Local sign data is [%s]", localSignature, signature, SignatureUtils.getSignData(accessData.getRequestUri(), accessData.getRequestParam(), accessData.getRequestHeader(), accessData.getRequestBody(), caller.getAppSecret()));
            log.error(errorInfo);
            throw new SignatureInvalidException(errorInfo);
        }
    }

    /**
     * 获取传入的signature
     *
     * @param accessData
     * @return
     */
    private String getSignature(AccessData accessData) {
        String signature = null;
        // 这里要用短杠才能取到
        if (accessData.getRequestHeader() != null) {
            signature = accessData.getRequestHeader().get(AccessData.SIGNATURE_PARAM_CODE);
        }
        if (StringUtils.isBlank(signature)) {
            signature = accessData.getRequestParam().get(AccessData.SIGNATURE_PARAM_CODE);
        }
        return signature;
    }
}
