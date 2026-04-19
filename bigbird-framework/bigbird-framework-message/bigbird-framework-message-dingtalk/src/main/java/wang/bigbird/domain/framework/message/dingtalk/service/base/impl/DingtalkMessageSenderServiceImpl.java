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
package wang.bigbird.domain.framework.message.dingtalk.service.base.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.message.api.exception.MessageSendException;
import wang.bigbird.domain.framework.message.api.service.base.impl.AbstractMessageSenderServiceImpl;

import java.io.File;
import java.util.Map;

/**
 * 钉钉消息发送器
 *
 * @author Bigbird
 */
@Slf4j
@Service("dingtalkMessageSenderService")
public class DingtalkMessageSenderServiceImpl extends AbstractMessageSenderServiceImpl {

    @Override
    protected String doSendMessage(String subject, String content, File[] attachFiles, String recipients, String copyRecipients) {
        throw new MessageSendException("Send message is not supported at this stage!");
    }

    @Override
    protected String doSendMessageByTemplate(String templateId, Map<String, String> params, String recipients, String copyRecipients) {
        throw new MessageSendException("Send message by template is not supported at this stage!");
    }
}
