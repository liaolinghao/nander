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
package wang.bigbird.domain.framework.message.sms.chinatelecom.service.base.impl;

import cn.hutool.crypto.digest.MD5;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.DateUtils;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.message.api.exception.MessageSendException;
import wang.bigbird.domain.framework.message.api.service.base.impl.AbstractMessageSenderServiceImpl;
import wang.bigbird.domain.framework.message.sms.chinatelecom.retrofit.IntegratedHttpClient;
import wang.bigbird.domain.framework.message.sms.chinatelecom.domain.dto.out.IntegratedSendSmsResponseDTO;
import wang.bigbird.domain.framework.message.sms.chinatelecom.retrofit.OpenHttpClient;
import wang.bigbird.domain.framework.message.sms.chinatelecom.domain.dto.out.OpenSendSmsResponseDTO;
import wang.bigbird.domain.framework.message.sms.chinatelecom.config.property.SmsProperties;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 短信消息发送器
 *
 * @author Bigbird
 */
@Slf4j
@Service("chinatelecomSmsMessageSenderService")
public class ChinatelecomSmsMessageSenderServiceImpl extends AbstractMessageSenderServiceImpl {

    @Autowired
    private SmsProperties smsProperties;

    @Autowired
    private OpenHttpClient openHttpClient;
    @Autowired
    private IntegratedHttpClient integratedHttpClient;

    @Override
    protected String doSendMessage(String subject, String content, File[] attachFiles, String recipients, String copyRecipients) {
        throw new MessageSendException("Send message is not supported at this stage!");
    }

    @Override
    protected String doSendMessageByTemplate(String templateId, Map<String, String> params, String recipients, String copyRecipients) {
        Set<String> mobilePhones = parsePhones(recipients, copyRecipients);
        Map<String, String> map = new HashMap<>(CollectionUtils.initialMapCapacity(mobilePhones.size()));
        for (String mobilePhone : mobilePhones) {
            String result = doSendMessageByTemplate(templateId, params, mobilePhone);
            map.put(mobilePhone, result);
        }
        String res = JsonUtils.object2Json(map);
        log.info("DoSendMessageByTemplate:{}", res);
        return res;
    }

    private String doSendMessageByTemplate(String templateId, Map<String, String> params, String mobilePhone) {
        switch (smsProperties.getType()) {
            case OPEN:
                return doSendMessageByTemplateFromOpen(templateId, params, mobilePhone);
            case INTEGRATED:
                return doSendMessageByTemplateFromIntegrated(templateId, params, mobilePhone);
            default:
                throw new MessageSendException("Send message is not supported at this stage!");
        }
    }

    /**
     * 依靠一体化服务平台发送短信
     *
     * @param templateId  模版ID
     * @param params      参数
     * @param mobilePhone 手机号
     * @return 发送结果
     */
    private String doSendMessageByTemplateFromIntegrated(String templateId, Map<String, String> params, String mobilePhone) {
        Map<String, String> map = new HashMap<>(CollectionUtils.initialMapCapacity(6));
        map.put("templateId", templateId);
        map.put("cpCode", smsProperties.getIntegrated().getCpCode());
        map.put("msg", loadMsg(params));
        map.put("mobiles", mobilePhone);
        map.put("exCode", "");
        map.put("sign", loadSign(map));
        try {
            IntegratedSendSmsResponseDTO integratedSendSmsResponse = integratedHttpClient.doSendMessageByTemplate(map).execute().body();
            if (integratedSendSmsResponse.getCode() != 0) {
                throw new MessageSendException(StringUtils.joinStr("Send message by template has error:", integratedSendSmsResponse.getMsg()));
            }
            return integratedSendSmsResponse.getTaskId();
        } catch (IOException e) {
            log.error("DoSendMessageByTemplateFromIntegrated:", e);
            throw new MessageSendException(StringUtils.joinStr("Send message by template has error:", e.getMessage()));
        }
    }

    private String loadSign(Map<String, String> map) {
        String signStr = StringUtils.joinStr(map.get("cpCode"), map.get("msg"), map.get("mobiles"), map.get("exCode"), map.get("templateId"), smsProperties.getIntegrated().getAccessKey());
        return MD5.create().digestHex(signStr).toLowerCase();
    }

    private String loadMsg(Map<String, String> params) {
        Collection<String> vs = params.values();
        return StringUtils.collectionToDelimitedString(vs, ",");
    }

    /**
     * 依靠能力开放平台发送短信
     *
     * @param templateId  模版ID
     * @param params      参数
     * @param mobilePhone 手机号
     * @return 发送结果
     */
    private String doSendMessageByTemplateFromOpen(String templateId, Map<String, String> params, String mobilePhone) {
        Map<String, String> map = new HashMap<>(CollectionUtils.initialMapCapacity(6));
        map.put("template_id", templateId);
        map.put("template_param", JsonUtils.object2Json(params));
        map.put("timestamp", DateUtils.getTimeStamp(false));
        map.put("access_token", smsProperties.getOpen().getToken());
        map.put("acceptor_tel", mobilePhone);
        map.put("app_id", smsProperties.getOpen().getAppId());
        OpenSendSmsResponseDTO sendSmsResponse = openHttpClient.doSendMessageByTemplate(map).body();
        if (sendSmsResponse.getRes_code() != 0) {
            throw new MessageSendException(StringUtils.joinStr("Send message by template has error:", sendSmsResponse.getRes_message()));
        }
        return sendSmsResponse.getIdertifier();
    }

    private Set<String> parsePhones(String recipients, String copyRecipients) {
        Set<String> mobilePhones = new HashSet<>();
        mobilePhones.addAll(Lists.newArrayList(recipients.split(",")));
        if (StringUtils.isNotBlank(copyRecipients)) {
            mobilePhones.addAll(Lists.newArrayList(copyRecipients.split(",")));
        }
        return mobilePhones;
    }
}
