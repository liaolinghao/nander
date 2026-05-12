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
package wang.bigbird.domain.framework.server.web.captcha.service.base.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.message.api.service.base.IMessageSenderService;
import wang.bigbird.domain.framework.server.web.captcha.config.property.CaptchaProperties;
import wang.bigbird.domain.framework.server.web.captcha.service.base.ISmsService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 短信服务
 *
 * @author Bigbird
 */
@Slf4j
@Service
public class SmsServiceImpl implements ISmsService {

    @Autowired
    private CaptchaProperties captchaProperties;
    @Autowired
    private IMessageSenderService chinatelecomSmsMessageSenderService;

    @Override
    public void sendSmsByTemplate(String mobilePhone, String number, String templateId) throws InterruptedException {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("captcha", number);
        params.put("ttl", String.valueOf(captchaProperties.getTtl() / 60));
        if (captchaProperties.getAsync()) {
            chinatelecomSmsMessageSenderService.sendMessageByTemplateByFrequency(templateId, params, mobilePhone, null);
        } else {
            chinatelecomSmsMessageSenderService.sendMessageByTemplate(templateId, params, mobilePhone, null);
        }
    }
}
