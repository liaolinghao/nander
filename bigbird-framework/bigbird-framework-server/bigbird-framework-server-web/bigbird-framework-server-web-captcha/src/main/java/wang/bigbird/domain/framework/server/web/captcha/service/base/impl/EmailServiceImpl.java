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
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.message.api.service.base.IMessageSenderService;
import wang.bigbird.domain.framework.server.web.captcha.config.property.CaptchaProperties;
import wang.bigbird.domain.framework.server.web.captcha.service.base.IEmailService;

/**
 * 邮箱服务
 *
 * @author Bigbird
 */
@Slf4j
@Service
public class EmailServiceImpl implements IEmailService {

    @Autowired
    private CaptchaProperties captchaProperties;
    @Autowired
    private IMessageSenderService emailMessageSenderService;

    @Override
    public void send(String email, String number, String template) throws InterruptedException {
        String[] ts = template.split("\\|");
        String subject = ts[0];
        StringBuffer contentTemplate = new StringBuffer();
        for (int i = 1; i < ts.length; i++) {
            contentTemplate.append(ts[i]).append(StringUtils.getLineSeparator());
        }
        String content = StringUtils.replacePlaceholders(contentTemplate.toString(), number, String.valueOf(captchaProperties.getTtl() / 60));
        if (captchaProperties.getAsync()) {
            emailMessageSenderService.sendMessageByFrequency(subject, content, null, email, null);
        } else {
            emailMessageSenderService.sendMessage(subject, content, null, email, null);
        }
    }

}
