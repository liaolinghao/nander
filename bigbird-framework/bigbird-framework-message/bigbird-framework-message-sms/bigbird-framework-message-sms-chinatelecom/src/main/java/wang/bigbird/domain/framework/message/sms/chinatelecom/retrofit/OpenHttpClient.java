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
package wang.bigbird.domain.framework.message.sms.chinatelecom.retrofit;

import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitClient;
import retrofit2.Response;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import wang.bigbird.domain.framework.message.sms.chinatelecom.domain.dto.out.OpenSendSmsResponseDTO;
import wang.bigbird.domain.framework.message.sms.chinatelecom.retrofit.fallback.OpenHttpDegradeFallbackFactory;

import java.util.Map;

/**
 * 中国电信能力开放平台接口调用器
 *
 * @author Bigbird
 * @RetrofitClient 只支持http接口，不支持https
 */
@RetrofitClient(baseUrl = "${bigbird.message.sms.open.baseUrl:http://api.189.cn}", fallbackFactory = OpenHttpDegradeFallbackFactory.class)
public interface OpenHttpClient {

    /**
     * 发送模版消息
     *
     * @param map 模版消息参数
     * @return 发送结果
     */
    @POST("/v2/emp/templateSms/sendSms")
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    Response<OpenSendSmsResponseDTO> doSendMessageByTemplate(@FieldMap Map<String, String> map);

}
