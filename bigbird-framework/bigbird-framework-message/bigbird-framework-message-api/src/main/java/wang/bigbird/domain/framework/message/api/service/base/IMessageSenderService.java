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
package wang.bigbird.domain.framework.message.api.service.base;

import java.io.File;
import java.util.Map;

/**
 * 消息发送接口
 *
 * @author Bigbird
 */
public interface IMessageSenderService {

    /**
     * 发送消息
     *
     * @param subject     消息主题
     * @param content     消息正文
     * @param attachFiles 附件
     * @param recipients     消息接收人，多个之间用,分隔
     * @param copyRecipients     消息抄收人，多个之间用,分隔
     * @return 发送结果
     */
    String sendMessage(String subject, String content,
                       File[] attachFiles, String recipients, String copyRecipients);

    /**
     * 按照消息平台定义的模板发送消息
     *
     * @param templateId 模板ID
     * @param params     消息参数
     * @param recipients    消息接收人，多个之间用,分隔
     * @param copyRecipients    消息抄收人，多个之间用,分隔
     * @return 发送结果
     */
    String sendMessageByTemplate(String templateId, Map<String, String> params, String recipients, String copyRecipients);

    /**
     * 按频率发送消息，当突然出现大规模消息时，会将消息排队并按照一定频率处理
     *
     * @param subject     消息主题
     * @param content     消息正文
     * @param attachFiles 附件
     * @param recipients     消息接收人，多个之间用,分隔
     * @param copyRecipients     消息抄收人，多个之间用,分隔
     * @throws InterruptedException
     */
    void sendMessageByFrequency(String subject, String content,
                                File[] attachFiles, String recipients, String copyRecipients) throws InterruptedException;

    /**
     * 按频率按照消息平台定义的模板发送消息，当突然出现大规模消息时，会将消息排队并按照一定频率处理
     *
     * @param templateId 模板ID
     * @param params     消息参数
     * @param recipients    消息接收人，多个之间用,分隔
     * @param copyRecipients    消息抄收人，多个之间用,分隔
     * @throws InterruptedException
     */
    void sendMessageByTemplateByFrequency(String templateId, Map<String, String> params, String recipients, String copyRecipients) throws InterruptedException;
}
