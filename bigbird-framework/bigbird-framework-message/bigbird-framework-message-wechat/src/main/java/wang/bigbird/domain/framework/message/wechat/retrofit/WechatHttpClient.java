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
package wang.bigbird.domain.framework.message.wechat.retrofit;

import retrofit2.Call;
import retrofit2.http.*;
import wang.bigbird.domain.framework.message.wechat.domain.dto.AccessTokenDTO;
import wang.bigbird.domain.framework.message.wechat.domain.dto.MessageSendDTO;
import wang.bigbird.domain.framework.message.wechat.domain.param.TextMessageParam;

/**
 * 企业微信平台接口调用器
 *
 * @author Bigbird
 */
public interface WechatHttpClient {

    /**
     * 获取企业微信授权token
     *
     * @param corpid     企业ID
     * @param corpsecret 企业密钥
     * @return 授权token
     */
    @GET("/cgi-bin/gettoken")
    Call<AccessTokenDTO> getToken(@Query("corpid") String corpid, @Query("corpsecret") String corpsecret);

    /**
     * 发送文本消息
     *
     * @param accessToken  授权token
     * @param messageParam 消息
     * @return 消息发送结果
     */
    @POST("/cgi-bin/message/send")
    @Headers("Content-Type: application/json;charset=UTF-8")
    Call<MessageSendDTO> sendTextMessage(@Query("access_token") String accessToken, @Body TextMessageParam messageParam);

}
