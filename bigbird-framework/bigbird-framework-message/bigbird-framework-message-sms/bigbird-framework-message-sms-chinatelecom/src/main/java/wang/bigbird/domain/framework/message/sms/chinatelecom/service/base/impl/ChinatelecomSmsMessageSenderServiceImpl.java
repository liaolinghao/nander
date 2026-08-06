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
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.util.DateUtils;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.message.api.exception.MessageSendException;
import wang.bigbird.domain.framework.message.api.service.base.impl.AbstractMessageSenderServiceImpl;
import wang.bigbird.domain.framework.message.sms.chinatelecom.config.property.SmsProperties;
import wang.bigbird.domain.framework.message.sms.chinatelecom.domain.dto.out.IntegratedSendSmsResponseDTO;
import wang.bigbird.domain.framework.message.sms.chinatelecom.domain.dto.out.OpenSendSmsResponseDTO;
import wang.bigbird.domain.framework.message.sms.chinatelecom.domain.dto.out.YunyixinSendSmsResponseDTO;
import wang.bigbird.domain.framework.message.sms.chinatelecom.retrofit.IntegratedHttpClient;
import wang.bigbird.domain.framework.message.sms.chinatelecom.retrofit.OpenHttpClient;
import wang.bigbird.domain.framework.message.sms.chinatelecom.retrofit.YunyixinHttpClient;

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
    @Autowired
    private YunyixinHttpClient yunyixinHttpClient;

    @Override
    protected String doSendMessage(String subject, String content, File[] attachFiles, String recipients, String copyRecipients) {
        Set<String> mobilePhones = parsePhones(recipients, copyRecipients);
        Map<String, String> map = Maps.newHashMapWithExpectedSize(mobilePhones.size());
        for (String mobilePhone : mobilePhones) {
            String result = doSendMessage(subject, content, mobilePhone);
            map.put(mobilePhone, result);
        }
        String res = JsonUtils.object2Json(map);
        log.info("DoSendMessage:{}", res);
        return res;
    }

    private String doSendMessage(String subject, String content, String mobilePhone) {
        switch (smsProperties.getType()) {
            case YUNYIXIN:
                return doSendMessageFromYunyixin(subject, content, mobilePhone);
            default:
                throw new MessageSendException("Send message is not supported at this stage!");
        }
    }

    /**
     * 依靠云翼信平台发送短信
     * <p>
     * 云翼信为原生短信平台，不支持模板发送。
     * 调用方如果需要记录短信发送状态，需要按照规则构造事务号作为 subject 传入。
     * 调用方需在业务层完成模板变量替换，将替换后的完整短信内容作为 content 传入。
     * <p>
     *
     * @param subject     短信事务号
     * @param content     短信内容
     * @param mobilePhone 手机号
     * @return 云翼信事务号，用于后续匹配回执
     */
    private String doSendMessageFromYunyixin(String subject, String content, String mobilePhone) {
        String transactionID;
        if (StringUtils.isNotBlank(subject)) {
            transactionID = subject;
        } else {
            transactionID = loadYunyixinTimeStamp();
        }
        SmsProperties.Yunyixin yunyixin = smsProperties.getYunyixin();
        String timeStamp = loadYunyixinTimeStamp();
        String streamingNo = yunyixin.getSiid() + transactionID;
        String authenticator = loadYunyixinAuthenticator(timeStamp, transactionID, streamingNo, yunyixin.getSecret());
        Map<String, String> map = Maps.newHashMapWithExpectedSize(9);
        map.put("siid", yunyixin.getSiid());
        map.put("user", yunyixin.getUser());
        map.put("streamingNo", streamingNo);
        map.put("timeStamp", timeStamp);
        map.put("transactionID", transactionID);
        map.put("authenticator", authenticator);
        map.put("mobile", mobilePhone);
        map.put("extcode", "");
        map.put("content", content);
        YunyixinSendSmsResponseDTO yunyixinSendSmsResponse = yunyixinHttpClient.doSendMessage(map).body();
        if (!YunyixinSendSmsResponseDTO.OK.equals(yunyixinSendSmsResponse.getRetCode())) {
            throw new MessageSendException(StringUtils.joinStr("Send message has error:", yunyixinSendSmsResponse.getRetMsg()));
        }
        return yunyixinSendSmsResponse.getTransactionID();
    }

    /**
     * 生成云翼信认证码
     * authenticator = BASE64(MD5(timeStamp + transactionID + streamingNo + secret))
     *
     * @param timeStamp     时间戳
     * @param transactionID 事务号
     * @param streamingNo   流水号
     * @param secret        接口密钥
     * @return 认证码
     */
    private String loadYunyixinAuthenticator(String timeStamp, String transactionID, String streamingNo, String secret) {
        String signStr = timeStamp + transactionID + streamingNo + secret;
        byte[] md5Bytes = MD5.create().digest(signStr);
        return Base64.getEncoder().encodeToString(md5Bytes);
    }

    /**
     * 生成云翼信时间戳，格式：yyyyMMddhhmmssSSS
     *
     * @return 时间戳
     */
    private String loadYunyixinTimeStamp() {
        Calendar cal = Calendar.getInstance();
        return String.format("%04d%02d%02d%02d%02d%02d%03d",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND),
                cal.get(Calendar.MILLISECOND));
    }

    @Override
    protected String doSendMessageByTemplate(String templateId, Map<String, String> params, String recipients, String copyRecipients) {
        Set<String> mobilePhones = parsePhones(recipients, copyRecipients);
        Map<String, String> map = Maps.newHashMapWithExpectedSize(mobilePhones.size());
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
        Map<String, String> map = Maps.newHashMapWithExpectedSize(6);
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
        Map<String, String> map = Maps.newHashMapWithExpectedSize(6);
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
