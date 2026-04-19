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
package wang.bigbird.domain.framework.message.email.service.base.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.message.api.exception.MessageSendException;
import wang.bigbird.domain.framework.message.api.service.base.impl.AbstractMessageSenderServiceImpl;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Map;

/**
 * 邮件消息发送器
 *
 * @author Bigbird
 */
@Slf4j
@Service("emailMessageSenderService")
public class EmailMessageSenderServiceImpl extends AbstractMessageSenderServiceImpl {

    @Value("${spring.mail.username}")
    protected String from;

    @Autowired
    protected JavaMailSender javaMailSender;

    @Override
    protected String doSendMessage(String subject, String content, File[] attachFiles, String recipients, String copyRecipients) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            // true 表示创建一个multipart message
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setSubject(subject);
            helper.setText(content, true);
            helper.setTo(recipients.split(","));
            if (attachFiles != null) {
                for (File attachFile : attachFiles) {
                    helper.addAttachment(attachFile.getName(), attachFile);
                }
            }
            if (StringUtils.isNotBlank(copyRecipients)) {
                helper.setCc(copyRecipients.split(","));
            }
            javaMailSender.send(message);
            return "";
        } catch (MessagingException e) {
            log.error("SendMessage:", e);
            throw new MessageSendException(e);
        }
    }

    @Override
    protected String doSendMessageByTemplate(String templateId, Map<String, String> params, String recipients, String copyRecipients) {
        throw new MessageSendException("Send message by template is not supported at this stage!");
    }

}
