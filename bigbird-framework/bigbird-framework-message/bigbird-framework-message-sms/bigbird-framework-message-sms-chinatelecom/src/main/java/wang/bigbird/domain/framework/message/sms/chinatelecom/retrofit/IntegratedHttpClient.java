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

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import wang.bigbird.domain.framework.message.sms.chinatelecom.domain.dto.out.IntegratedSendSmsResponseDTO;

import java.util.Map;

/**
 * 中国电信一体化服务平台接口调用器
 *
 * @author Bigbird
 */
public interface IntegratedHttpClient {

    /**
     * 发送模版消息
     *
     * @param map 模版消息参数
     * @return 发送结果
     */
    @POST("/integratedmsg/sms/sendtempletmsg")
    @Headers("Content-Type: application/json;charset=UTF-8")
    Call<IntegratedSendSmsResponseDTO> doSendMessageByTemplate(@Body Map<String, String> map);

}
