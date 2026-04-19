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
package wang.bigbird.domain.framework.message.api.service.base.impl;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.message.api.base.pojo.AbstractMessage;
import wang.bigbird.domain.framework.message.api.base.pojo.CustomMessage;
import wang.bigbird.domain.framework.message.api.base.pojo.TemplateMessage;
import wang.bigbird.domain.framework.message.api.service.base.IMessageSenderService;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 抽象的消息发送器
 *
 * @author Bigbird
 */
@Slf4j
public abstract class AbstractMessageSenderServiceImpl implements IMessageSenderService {

    /**
     * 存放运行过程中的提示信息
     */
    private final LinkedBlockingQueue<AbstractMessage> messageQueue = new LinkedBlockingQueue<>(10000);

    private final ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
            .setNamePrefix("message-start-thread-").build();
    private final ExecutorService threadPoolExecutor = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

    /**
     * 是否启动发送器
     */
    private volatile boolean started = false;

    /**
     * 是否关闭
     */
    private volatile boolean destroyed = false;

    /**
     * 毫秒为单位
     */
    @Value("${bigbird.message.frequency:1000}")
    private long frequency = 1000L;

    @PreDestroy
    public void destroy() {
        destroyed = true;
        threadPoolExecutor.shutdown();
        log.info("Destroyed message sender.");
    }

    @Override
    public String sendMessage(String subject, String content, File[] attachFiles, String recipients, String copyRecipients) {
        Assert.isTrue(StringUtils.isNotBlank(subject), "The subject is blank.");
        Assert.isTrue(StringUtils.isNotBlank(content), "The content is blank.");
        Assert.isTrue(StringUtils.isNotBlank(recipients), "The recipients is blank.");
        try {
            return doSendMessage(subject, content, attachFiles, recipients, copyRecipients);
        } catch (Exception e) {
            log.error("SendMessage:", e);
            return "";
        }
    }

    @Override
    public String sendMessageByTemplate(String templateId, Map<String, String> params, String recipients, String copyRecipients) {
        Assert.isTrue(StringUtils.isNotBlank(templateId), "The templateId is blank.");
        Assert.notNull(params, "The params is null.");
        Assert.isTrue(StringUtils.isNotBlank(recipients), "The recipients is blank.");
        try {
            return doSendMessageByTemplate(templateId, params, recipients, copyRecipients);
        } catch (Exception e) {
            log.error("SendMessageByTemplate:", e);
            return "";
        }
    }

    @Override
    public void sendMessageByFrequency(String subject, String content,
                                       File[] attachFiles, String recipients, String copyRecipients) throws InterruptedException {
        Assert.isTrue(StringUtils.isNotBlank(subject), "The subject is blank.");
        Assert.isTrue(StringUtils.isNotBlank(content), "The content is blank.");
        Assert.isTrue(StringUtils.isNotBlank(recipients), "The recipients is blank.");
        CustomMessage customMessage = new CustomMessage(subject, content, attachFiles);
        customMessage.setRecipients(recipients);
        customMessage.setCopyRecipients(copyRecipients);
        messageQueue.put(customMessage);
        start();
    }

    @Override
    public void sendMessageByTemplateByFrequency(String templateId, Map<String, String> params, String recipients, String copyRecipients) throws InterruptedException {
        Assert.isTrue(StringUtils.isNotBlank(templateId), "The templateId is blank.");
        Assert.notNull(params, "The params is null.");
        Assert.isTrue(StringUtils.isNotBlank(recipients), "The recipients is blank.");
        TemplateMessage templateMessage = new TemplateMessage(templateId, params);
        templateMessage.setRecipients(recipients);
        templateMessage.setCopyRecipients(copyRecipients);
        messageQueue.put(templateMessage);
        start();
    }

    private void start() {
        if (started) {
            return;
        }
        synchronized (messageQueue) {
            if (started) {
                return;
            }
            started = true;
            threadPoolExecutor.execute(() -> {
                while (!destroyed && !Thread.currentThread().isInterrupted()) {
                    try {
                        AbstractMessage message = messageQueue.take();
                        if (message instanceof TemplateMessage) {
                            doSendMessage((TemplateMessage) message);
                        } else if (message instanceof CustomMessage) {
                            doSendMessage((CustomMessage) message);
                        }
                        Thread.sleep(frequency);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Message sending thread was interrupted:", e);
                    } catch (Exception e) {
                        log.error("DoSendMessage:", e);
                    }
                }
            });
        }
    }

    private void doSendMessage(CustomMessage message) throws IOException {
        doSendMessage(message.getSubject(), message.getContent(), message.getAttachFiles(), message.getRecipients(), message.getCopyRecipients());
    }

    private void doSendMessage(TemplateMessage message) {
        doSendMessageByTemplate(message.getTemplateId(), message.getParams(), message.getRecipients(), message.getCopyRecipients());
    }

    /**
     * 执行发送消息
     *
     * @param subject        消息主题
     * @param content        消息正文
     * @param attachFiles    附件
     * @param recipients     消息接收人，多个之间用,分隔
     * @param copyRecipients 消息抄收人，多个之间用,分隔
     * @return 发送结果
     * @throws IOException
     */
    protected abstract String doSendMessage(String subject, String content,
                                            File[] attachFiles, String recipients, String copyRecipients) throws IOException;

    /**
     * 按照消息平台定义的模板发送消息
     *
     * @param templateId     模板ID
     * @param params         消息参数
     * @param recipients     消息接收人，多个之间用,分隔
     * @param copyRecipients 消息抄收人，多个之间用,分隔
     * @return 发送结果
     */
    protected abstract String doSendMessageByTemplate(String templateId, Map<String, String> params, String recipients, String copyRecipients);

}
