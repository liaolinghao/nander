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
package wang.bigbird.domain.framework.message.wechat.service.base.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.message.api.exception.MessageSendException;
import wang.bigbird.domain.framework.message.api.service.base.impl.AbstractMessageSenderServiceImpl;
import wang.bigbird.domain.framework.message.wechat.domain.dto.AccessTokenDTO;
import wang.bigbird.domain.framework.message.wechat.domain.dto.MessageSendDTO;
import wang.bigbird.domain.framework.message.wechat.domain.param.TextMessageParam;
import wang.bigbird.domain.framework.message.wechat.retrofit.WechatHttpClient;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 微信消息发送器
 *
 * @author Bigbird
 */
@Slf4j
@Service("wechatMessageSenderService")
public class WechatMessageSenderServiceImpl extends AbstractMessageSenderServiceImpl {

    @Value("${bigbird.message.wechat.corpId}")
    private String corpId;
    @Value("${bigbird.message.wechat.secret}")
    private String secret;
    @Value("${bigbird.message.wechat.agentId}")
    private int agentId;
    @Value("${bigbird.message.wechat.expires}")
    private int expires;

    @Autowired
    private WechatHttpClient wechatHttpClient;

    private Cache<String, String> cache;

    @PostConstruct
    public void init() {
        cache = CacheBuilder.newBuilder().expireAfterWrite(expires, TimeUnit.SECONDS).build();
    }

    @Override
    protected String doSendMessage(String subject, String content, File[] attachFiles, String recipients, String copyRecipients) throws IOException {
        String toUser = parseToUser(recipients, copyRecipients);
        TextMessageParam.Text text;
        if (StringUtils.isNotBlank(subject)) {
            text = new TextMessageParam.Text(StringUtils.joinStr(subject, System.getProperty("line.separator"), content));
        } else {
            text = new TextMessageParam.Text(content);
        }
        TextMessageParam textMessage = TextMessageParam.builder().touser(toUser).msgtype("text").agentid(agentId).text(text).build();
        String accessToken = cache.getIfPresent("accessToken");
        if (StringUtils.isBlank(accessToken)) {
            AccessTokenDTO accessTokenDTO = wechatHttpClient.getToken(corpId, secret).execute().body();
            accessToken = accessTokenDTO.getAccess_token();
            cache.put("accessToken", accessToken);
        }
        MessageSendDTO result = wechatHttpClient.sendTextMessage(accessToken, textMessage).execute().body();
        return JsonUtils.object2Json(result);
    }

    @Override
    protected String doSendMessageByTemplate(String templateId, Map<String, String> params, String recipients, String copyRecipients) {
        throw new MessageSendException("Send message by template is not supported at this stage!");
    }

    private String parseToUser(String recipients, String copyRecipients) {
        Set<String> toUsers = new HashSet<>();
        toUsers.addAll(Lists.newArrayList(recipients.split(",")));
        if (StringUtils.isNotBlank(copyRecipients)) {
            toUsers.addAll(Lists.newArrayList(copyRecipients.split(",")));
        }
        return StringUtils.collectionToDelimitedString(toUsers, "|");
    }
}
